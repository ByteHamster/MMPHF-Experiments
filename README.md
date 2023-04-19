# MMPHF-Experiments

Monotone Minimal Perfect Hashing competitors.

## Reproducing Experiments

This repository contains the source code and our reproducibility artifacts for comparing different MMPHF constructions.

We provide an easy to use Docker image to quickly reproduce our results.
Alternatively, you can look at the `Dockerfile` to see all libraries, tools, and commands necessary to compile.

#### Cloning the Repository

This repository contains submodules.
To clone the repository including submodules, use the following command.

```
git clone --recursive https://github.com/ByteHamster/MMPHF-Experiments.git
```

#### Building the Docker Image

Run the following command to build the Docker image.
Building the image takes about 10 minutes, as some packages (including LaTeX for the plots) have to be installed.

```bash
docker build -t mmphf_experiments --no-cache .
```

Some compiler warnings (red) are expected when building dependencies and will not prevent building the image or running the experiments.
Please ignore them!

#### Running the Experiments
Due to the long total running time of all experiments in our paper, we provide a run script for a highly simplified version of the experiments.
Most importantly, we use a small, synthetic dataset (also due to licensing and download size).

You can modify the benchmark scripts in `scripts/dockerVolume` if you want to change any parameters.
This does not require the Docker image to recompile.
The experiments can be started by using the following command:

```bash
docker run --interactive --tty -v "$(pwd)/scripts/dockerVolume:/opt/dockerVolume" mmphf_experiments /opt/dockerVolume/normal-distribution.sh
```

The resulting plots can be found in `scripts/dockerVolume` and have the file extension `.pdf`.

### License

The benchmark code is licensed under the [GPLv3](/LICENSE).
The competitors (in the `cpp/extlib` and `java/extlib` directories) are licensed with their respective licenses.
