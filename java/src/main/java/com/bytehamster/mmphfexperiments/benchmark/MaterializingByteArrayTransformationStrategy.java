package com.bytehamster.mmphfexperiments.benchmark;

import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.bits.TransformationStrategy;

/**
 * A prefix-free byte-array transformation strategy that directly materializes
 * the bit vector into a {@link LongArrayBitVector}, rather than returning a
 * lazy adaptor.
 *
 * <p>This avoids the per-query allocation that occurs when the MMPHF code calls
 * {@link BitVector#fast()} on the lazy {@code ByteArrayBitVector} returned by
 * {@link it.unimi.dsi.bits.TransformationStrategies#prefixFreeByteArray()}.
 * Since {@link LongArrayBitVector#fast()} returns {@code this}, no further
 * copying is needed.
 */
public class MaterializingByteArrayTransformationStrategy implements TransformationStrategy<byte[]> {
	private static final long serialVersionUID = 1L;

	private static final MaterializingByteArrayTransformationStrategy INSTANCE =
		new MaterializingByteArrayTransformationStrategy();

	public static TransformationStrategy<byte[]> getInstance() {
		return INSTANCE;
	}

	private MaterializingByteArrayTransformationStrategy() {}

	/** Reverses the bit order within each byte of a 64-bit word, converting
	 *  between MSB-first (lexicographic) and LSB-first ({@link LongArrayBitVector}) conventions. */
	private static long reverseBytes(long word) {
		word = (word & 0x5555555555555555L) << 1 | (word >>> 1) & 0x5555555555555555L;
		word = (word & 0x3333333333333333L) << 2 | (word >>> 2) & 0x3333333333333333L;
		return (word & 0x0f0f0f0f0f0f0f0fL) << 4 | (word >>> 4) & 0x0f0f0f0f0f0f0f0fL;
	}

	@Override
	public BitVector toBitVector(final byte[] a) {
		final long length = a.length * 8L + 8; // +8 for prefix-free NUL terminator
		final int numWords = (int)((length + Long.SIZE - 1) >>> 6);
		final long[] bits = new long[numWords];

		final int fullWords = a.length >>> 3;
		int pos = 0;
		for (int w = 0; w < fullWords; w++) {
			bits[w] = reverseBytes(
				(a[pos] & 0xFFL) |
				(a[pos + 1] & 0xFFL) << 8 |
				(a[pos + 2] & 0xFFL) << 16 |
				(a[pos + 3] & 0xFFL) << 24 |
				(a[pos + 4] & 0xFFL) << 32 |
				(a[pos + 5] & 0xFFL) << 40 |
				(a[pos + 6] & 0xFFL) << 48 |
				(a[pos + 7] & 0xFFL) << 56
			);
			pos += 8;
		}

		// Remaining bytes (0-7) in the last partial word
		if (pos < a.length) {
			long word = 0;
			for (int i = 0; pos + i < a.length; i++) {
				word |= (a[pos + i] & 0xFFL) << (i * 8);
			}
			bits[fullWords] = reverseBytes(word);
		}
		// Trailing 8 zero bits for prefix-freeness are already 0 in the array.

		return LongArrayBitVector.wrap(bits, length);
	}

	@Override
	public long length(final byte[] a) {
		return a.length * 8L;
	}

	@Override
	public long numBits() {
		return 0;
	}

	@Override
	public TransformationStrategy<byte[]> copy() {
		return this;
	}

	private Object readResolve() {
		return INSTANCE;
	}
}
