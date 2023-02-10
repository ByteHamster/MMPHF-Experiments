# MMPHF-Experiments

Monotone Minimal Perfect Hashing competitors.

### Cloning

```
git clone git@github.com:ByteHamster/MMPHF-Experiments.git
git submodule update --recursive --init 
```

### Running C++ Competitors

```
mkdir cpp/build
cd cpp/build
cmake -DCMAKE_BUILD_TYPE=Release ..
make
./Comparison --help
```

### Running Java Competitors

```
apt install ivy ant
cd java
mvn package
java -jar target/MmphfExperiments-1.0-jar-with-dependencies.jar --help
```

### Generating Plots

```
cd plots
make plots
```

If you have new plot data to plot, you need to install `sqlplot-tools` and run `make plotdata`.

### License

The benchmark code is licensed under the [GPLv3](/LICENSE).
The competitors (in the `cpp/extlib` and `java/extlib` directories) are licensed with their respective licenses.
