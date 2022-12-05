# MMPHF-Experiments

Monotone Minimal Perfect Hashing competitors.

### Building

For whatever reason, the `ot/succinct` repo cannot be cloned using `submodule update` on my machine.
Cloning it manually works. So you can run something like this:

```
git clone git@github.com:ByteHamster/MMPHF-Experiments.git
git submodule update --recursive --init 
# This will hang forever at ot/succinct. Just cancel.
git clone git@github.com:ot/succinct.git extlib/pathCompressedTries/succinct
git submodule update --recursive --init 
# This time it works...
```

### License

The benchmark code is licensed under the [GPLv3](/LICENSE).
The competitors (in the `extlib` directory) are licensed with their respective licenses.
