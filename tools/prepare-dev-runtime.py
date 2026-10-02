#!/usr/bin/env python3
"""Prepare or verify six isolated dev run directories; never launch Minecraft.

The selected runClient module supplies its own development classes. Its mods/
directory receives only the other built products, exact map host(s), and JEI.
Fabric API is supplied by Loom's existing modImplementation runtime classpath.
"""

from __future__ import annotations

import argparse
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tomllib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
WORKSPACE = ROOT.parent
VERSION = "1.21.1-0.1.0"
HOSTS = {
    "fabric": {
        "journeymap": ("journeymap-fabric-1.21.1-6.0.9.jar", "4e820703a90d1aad93e0090f9b9f5a57cfc51cf692c7b79d782fbe18e51f313a"),
        "xaero": ("xaerominimap-fabric-1.21.1-26.5.0.jar", "2109c24a5044a6bbce5c456292ce34de830ab35602f1940a700b9abc74537482"),
    },
    "neoforge": {
        "journeymap": ("journeymap-neoforge-1.21.1-6.0.9.jar", "9bd7734542233d5281750572f17a9258449e826650c081e9a67dc426262c2fde"),
        "xaero": ("xaerominimap-neoforge-1.21.1-26.5.0.jar", "667105d8fdca64b27b29d8e0b88e67e7342038d3fb4edc4129bf452b601c6d20"),
    },
}
JEI_CANDIDATES = {
    "fabric": ((WORKSPACE / "_tools/devmods/jei-1.21.1-fabric-19.27.0.336.jar",
                "1d47dab86cee6d68e0887ec5e14deee125d3e2ea92c5ecedbb57f8f60aafa709"),),
    "neoforge": (
        (WORKSPACE / "_tools/devmods/jei-1.21.1-neoforge-19.27.0.336.jar",
         "2b6d84df11ab2bb94a75be8bac6790bfbb2c0112a1622cd5f5f234468a196721"),
    ),
}
TARGET_VERSIONS = {"minecraft": "1.21.1", "neoforge": "21.1.227", "fabricloader": "0.16.9",
                   "fabric-api": "0.109.0", "java": "21", "javafml": "4"}
SCENARIOS = tuple((loader, name) for loader in ("fabric", "neoforge") for name in ("journeymap", "xaero", "both"))
MANIFEST = ".minimapshapes-dev-runtime.json"


def digest(path: Path) -> str:
    sha = hashlib.sha256()
    with path.open("rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            sha.update(block)
    return sha.hexdigest()


def version_numbers(value: str) -> tuple[int, ...]:
    match = re.fullmatch(r"(\d+(?:\.\d+)*)(?:\+[^\s]+)?", value.strip())
    if match is None:
        raise ValueError(f"Unsupported version in JEI metadata: {value!r}")
    return tuple(int(part) for part in match.group(1).split("."))


def compare_versions(left: str, right: str) -> int:
    a, b = version_numbers(left), version_numbers(right)
    length = max(len(a), len(b))
    return (a + (0,) * (length - len(a)) > b + (0,) * (length - len(b))) - (
        a + (0,) * (length - len(a)) < b + (0,) * (length - len(b)))


def accepts_range(spec: str, version: str) -> bool:
    spec = spec.strip()
    if spec == "*":
        return True
    if spec.startswith(">="):
        return compare_versions(version, spec[2:]) >= 0
    if spec.startswith("[") or spec.startswith("("):
        if not spec.endswith(("]", ")")):
            raise ValueError(f"Malformed JEI version range: {spec!r}")
        body = spec[1:-1]
        if "," not in body:
            if spec[0] != "[" or spec[-1] != "]":
                raise ValueError(f"Malformed exact JEI version: {spec!r}")
            return compare_versions(version, body) == 0
        lower, upper = (part.strip() for part in body.split(",", 1))
        if lower and (compare_versions(version, lower) < 0 or
                      (compare_versions(version, lower) == 0 and spec[0] == "(")):
            return False
        if upper and (compare_versions(version, upper) > 0 or
                      (compare_versions(version, upper) == 0 and spec[-1] == ")")):
            return False
        return True
    return compare_versions(version, spec) == 0


def validate_jei_metadata(path: Path, loader: str) -> None:
    """Reject a pinned JEI file whose own required dependencies exclude this dev run."""
    with zipfile.ZipFile(path) as archive:
        if loader == "fabric":
            metadata = json.loads(archive.read("fabric.mod.json"))
            if metadata.get("id") != "jei":
                raise RuntimeError(f"Fabric JEI mod id is not jei: {path}")
            depends = metadata.get("depends", {})
            for dependency in ("fabricloader", "fabric-api", "java"):
                spec = depends.get(dependency)
                if not isinstance(spec, str) or not accepts_range(spec, TARGET_VERSIONS[dependency]):
                    raise RuntimeError(f"Fabric JEI requires incompatible {dependency} {spec!r}: {path}")
            minecraft_spec = depends.get("minecraft")
            if minecraft_spec is not None and (not isinstance(minecraft_spec, str) or
                                               not accepts_range(minecraft_spec, TARGET_VERSIONS["minecraft"])):
                raise RuntimeError(f"Fabric JEI excludes Minecraft 1.21.1: {path}")
            # This pinned Fabric JAR has no Minecraft dependency field; its filename,
            # exact SHA-256 and loader requirements are the available static evidence.
        else:
            metadata = tomllib.loads(archive.read("META-INF/neoforge.mods.toml").decode("utf-8"))
            if not any(mod.get("modId") == "jei" for mod in metadata.get("mods", [])):
                raise RuntimeError(f"NeoForge JEI mod id is not jei: {path}")
            loader_spec = metadata.get("loaderVersion")
            if not isinstance(loader_spec, str) or not accepts_range(loader_spec, TARGET_VERSIONS["javafml"]):
                raise RuntimeError(f"NeoForge JEI excludes javafml {TARGET_VERSIONS['javafml']}: {path}")
            required = {entry.get("modId"): entry.get("versionRange")
                        for entry in metadata.get("dependencies", {}).get("jei", [])
                        if entry.get("type") == "required"}
            for dependency in ("minecraft", "neoforge"):
                spec = required.get(dependency)
                if not isinstance(spec, str) or not accepts_range(spec, TARGET_VERSIONS[dependency]):
                    raise RuntimeError(f"NeoForge JEI excludes {dependency} {TARGET_VERSIONS[dependency]} "
                                       f"(requires {spec!r}): {path}")
            unsupported = sorted(set(required) - {"minecraft", "neoforge"})
            if unsupported:
                raise RuntimeError(f"NeoForge JEI needs unstaged required mods {unsupported}: {path}")


def select_jei(loader: str) -> tuple[Path, str]:
    rejected = []
    for path, expected in JEI_CANDIDATES[loader]:
        try:
            if not path.is_file() or digest(path) != expected:
                raise RuntimeError("missing or SHA-256 mismatch")
            validate_jei_metadata(path, loader)
            return path, expected
        except (RuntimeError, ValueError, KeyError, zipfile.BadZipFile, tomllib.TOMLDecodeError) as error:
            rejected.append(f"{path.name}: {error}")
    raise RuntimeError(f"No compatible {loader} JEI for MC 1.21.1; " + "; ".join(rejected))


def java_env() -> dict[str, str]:
    env = dict(os.environ)
    if not env.get("JAVA_HOME"):
        mac_jdk = Path("/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home")
        if mac_jdk.is_dir():
            env["JAVA_HOME"] = str(mac_jdk)
    return env


def gradle_report() -> tuple[dict[str, Path], str]:
    command = [str(ROOT / "gradlew"), "help", ":core-fabric:dependencies", "--configuration", "modImplementation",
               "-I", str(ROOT / "tools/inspect-run-dirs.gradle"), "--no-daemon"]
    result = subprocess.run(command, cwd=ROOT, env=java_env(), capture_output=True, text=True, timeout=180)
    if result.returncode:
        raise RuntimeError("Gradle run-directory/Fabric API inspection failed:\n" + result.stdout[-2500:] + result.stderr[-2500:])
    run_dirs = {}
    for line in result.stdout.splitlines():
        if line.startswith("MMS_RUN_DIR|"):
            _, task, path = line.split("|", 2)
            run_dirs[task] = Path(path)
    if len(run_dirs) != 6:
        raise RuntimeError(f"Expected six Gradle run directories; found {len(run_dirs)}")
    fabric_api = "net.fabricmc.fabric-api:fabric-api:0.109.0+1.21.1"
    if fabric_api not in result.stdout:
        raise RuntimeError("Fabric API is missing from Loom modImplementation runtime dependencies")
    return run_dirs, fabric_api


def built_jar(product: str, loader: str) -> Path:
    name = f"minimapshapes_{product}-{product}-{loader}-{VERSION}.jar"
    path = ROOT / product / loader / "build/libs" / name
    if not path.is_file():
        raise RuntimeError(f"Build first: {path}")
    metadata = "fabric.mod.json" if loader == "fabric" else "META-INF/neoforge.mods.toml"
    with zipfile.ZipFile(path) as archive:
        if metadata not in archive.namelist():
            raise RuntimeError(f"Missing {metadata} in {path}")
        contents = archive.read(metadata).decode("utf-8")
        if f"minimapshapes_{product}" not in contents:
            raise RuntimeError(f"Wrong mod id in {path}")
    return path


def source_jars(loader: str, scenario: str) -> tuple[str, dict[str, tuple[Path, str]]]:
    active = "core" if scenario == "both" else scenario
    host_names = ("journeymap", "xaero") if scenario == "both" else (scenario,)
    staged: dict[str, tuple[Path, str]] = {}
    products = host_names if scenario == "both" else ("core",)
    for product in products:
        path = built_jar(product, loader)
        staged[path.name] = (path, digest(path))
    for host in host_names:
        filename, expected = HOSTS[loader][host]
        path = ROOT / "libs-local" / filename
        if not path.is_file() or digest(path) != expected:
            raise RuntimeError(f"Exact host JAR missing or modified: {path}")
        staged[filename] = (path, expected)
    path, expected = select_jei(loader)
    staged[path.name] = (path, expected)
    return active, staged


def check_directory(run_dir: Path, staged: dict[str, tuple[Path, str]], prepare: bool) -> None:
    mods = run_dir / "mods"
    if run_dir.is_symlink() or mods.is_symlink():
        raise RuntimeError(f"Refusing symlinked run directory: {run_dir}")
    if mods.exists() and not mods.is_dir():
        raise RuntimeError(f"Expected a mods directory: {mods}")
    if mods.is_dir():
        unexpected = sorted(p.name for p in mods.iterdir() if p.suffix == ".jar" and p.name not in staged)
        if unexpected:
            raise RuntimeError(f"Unexpected JARs in {mods}; inspect manually (nothing was deleted): {unexpected}")
        for name, (_, expected) in staged.items():
            dest = mods / name
            temporary = mods / (name + ".tmp")
            if temporary.exists():
                raise RuntimeError(f"Interrupted staging file, left untouched: {temporary}")
            if dest.exists() and (not dest.is_file() or digest(dest) != expected):
                raise RuntimeError(f"Conflicting existing file, left untouched: {dest}")
    elif not prepare:
        raise RuntimeError(f"Not prepared: {mods}")


def selected_scenarios(value: str) -> tuple[tuple[str, str], ...]:
    if value == "all":
        return SCENARIOS
    loader, name = value.split("-", 1)
    return ((loader, name),)


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("action", choices=("prepare", "verify"))
    parser.add_argument("scenario", nargs="?", default="all",
                        choices=("all", *(f"{loader}-{name}" for loader, name in SCENARIOS)))
    parser.add_argument("--owner", default=os.environ.get("RUNCLIENT_OWNER", "minimap-shapes-20261003"),
                        help="Owner tag for a later guarded client run; re-prepare when the session changes")
    args = parser.parse_args()
    if not re.fullmatch(r"[A-Za-z0-9._-]{1,64}", args.owner):
        raise ValueError("Owner tag must be 1-64 letters, digits, dots, underscores or hyphens")
    chosen = selected_scenarios(args.scenario)
    sources = []
    for loader, scenario in chosen:
        active, staged = source_jars(loader, scenario)
        sources.append((loader, scenario, active, staged))
    # Resolve Gradle only after every source JAR has passed compatibility checks.
    run_dirs, fabric_api = gradle_report()
    plans = []
    for loader, scenario, active, staged in sources:
        task = f":{active}-{loader}:runClient"
        run_dir = run_dirs[task.rsplit(":", 1)[0]]
        check_directory(run_dir, staged, args.action == "prepare")
        plans.append((loader, scenario, task, run_dir, staged))

    for loader, scenario, task, run_dir, staged in plans:
        mods = run_dir / "mods"
        manifest_path = run_dir / MANIFEST
        manifest = {
            "scenario": f"{loader}-{scenario}", "task": task,
            "supplied_by_run_task": f"minimapshapes_{task.split(':')[1].rsplit('-', 1)[0]}",
            "workdir": str(ROOT),
            "guarded_wrapper": str(WORKSPACE / "_tools/runclient_fresh.sh"),
            "launch_env": {"RUNCLIENT_OWNER": args.owner, "RUNCLIENT_NO_JEI": "1"},
            "loader": loader, "minecraft": "1.21.1",
            "fabric_api": fabric_api if loader == "fabric" else None,
            "fabric_api_provision": "Loom modImplementation runtime classpath" if loader == "fabric" else None,
            "files": {name: expected for name, (_, expected) in sorted(staged.items())},
        }
        if args.action == "prepare":
            mods.mkdir(parents=True, exist_ok=True)
            for name, (source, expected) in staged.items():
                target = mods / name
                if not target.exists():
                    temporary = mods / (name + ".tmp")
                    with temporary.open("xb") as stream, source.open("rb") as input_stream:
                        shutil.copyfileobj(input_stream, stream)
                    if digest(temporary) != expected:
                        raise RuntimeError(f"Copy changed during staging: {source}")
                    os.link(temporary, target)  # Fails instead of replacing a file created concurrently.
                    temporary.unlink()
            manifest_path.write_text(json.dumps(manifest, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
        if not manifest_path.is_file() or json.loads(manifest_path.read_text(encoding="utf-8")) != manifest:
            raise RuntimeError(f"Missing or stale manifest: {manifest_path}")
        check_directory(run_dir, staged, False)
        missing = [name for name in staged if not (mods / name).is_file()]
        if missing:
            raise RuntimeError(f"Missing staged JARs in {mods}: {missing}")
        print(f"OK {loader}-{scenario}: {task} -> {run_dir} ({len(staged)} staged JARs)")
        print(f"   later: RUNCLIENT_OWNER={args.owner} RUNCLIENT_NO_JEI=1 "
              f"{WORKSPACE / '_tools/runclient_fresh.sh'} {ROOT} {task}")
    print("No game client was launched. Future launch must use the guarded runclient_fresh.sh with RUNCLIENT_NO_JEI=1 and an owner tag.")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, RuntimeError, subprocess.TimeoutExpired, ValueError, zipfile.BadZipFile) as error:
        print(f"dev runtime setup failed: {error}", file=sys.stderr)
        raise SystemExit(1)
