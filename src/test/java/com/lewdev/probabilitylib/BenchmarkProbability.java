package com.lewdev.probabilitylib;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Benchmark)
@Fork(value = 2, jvmArgs = {"-Xms2G", "-Xmx2G"})
public class BenchmarkProbability {
	public int elements = 1_000;
	
	public int toAdd = elements + 1;
	public int toAddProb = 10;

	private ProbabilityCollection<Integer> collection;
	
	@Setup(Level.Iteration)
	public void setup() {
		this.collection = new ProbabilityCollection<>();
		
		for(int i = 0; i < elements; i++) {
			collection.add(i, 1);
		}
	}
	
	@TearDown(Level.Iteration)
	public void tearDown() {
		this.collection.clear();

		this.collection = null;
	}
	
	@Benchmark
	public void collectionAddSingle() {
		this.collection.add(toAdd, toAddProb);
	}
	
	@Benchmark
	public void collectionGet(Blackhole bh) {
		bh.consume(this.collection.get());
	}
}
