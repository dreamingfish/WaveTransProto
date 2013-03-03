package info.dreamingfish123.WaveTransProto.codec;

public class WaveDecoder {

	/*
	 * Decode self-defined wave format to data
	 */
	public static byte[] decode(byte[] wavein, int offset, int len) {
		byte[] ret = new byte[len / Constant.POINT_PER_BIT
				/ Constant.BIT_PER_BYTE];
		int retOffset = offset;
		int bit = 0;

		for (int i = 0; i < ret.length; i++) {
			ret[i] = 0;

			bit = convertBit(wavein, retOffset);
			retOffset += Constant.POINT_PER_BIT;

			for (int j = 0; j < 8; j++) {
				bit = convertBit(wavein, retOffset);
				retOffset += Constant.POINT_PER_BIT;

				ret[i] = (byte) (ret[i] + (bit << (7 - j)) & 0xFF);
			}

			bit = convertBit(wavein, retOffset);
			retOffset += Constant.POINT_PER_BIT;

		}

		return ret;
	}

	/*
	 * Get bit from wave format
	 */
	public static int convertBit(byte[] wavein, int offset) {
		int ave1 = 0;
		int ave2 = 0;

		for (int i = 0; i < Constant.POINT_PER_BIT_HALF; i++) {
			ave1 += 0xff & wavein[offset + i];
			ave2 += 0xff & wavein[offset + i + Constant.POINT_PER_BIT_HALF];
		}

		if (ave1 - ave2 > Constant.WAVE_DIFF_SUM_LEVEL) {
			return 0;
		} else if (ave2 - ave1 > Constant.WAVE_DIFF_SUM_LEVEL) {
			return 1;
		} else {
			return -1;
		}
	}

	/*
	 * Find the first byte of data:0xff
	 */
	private static int locateDataStartSignal(byte[] wavein) {
		final int half = Constant.POINT_PER_BIT / 2;
		for (int i = 0; i < wavein.length - half; i++) {
			if (wavein[i] >= wavein[i + half] + Constant.WAVE_DIFF_LEVEL) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * decode an entire UART data
	 * 
	 * @param data
	 *            data buffer to be decoded
	 * @param offset
	 *            start from
	 * @return >= 0 if decode succeed and the result should be returned; < 0 if
	 *         error occurred
	 */
	public static int decodeUART(byte[] data, int offset) {
		int offsetTmp = offset;
		int retTmp = 0;
		int ret = 0;

		retTmp = WaveDecoder.convertBit(data, offsetTmp);
		if (retTmp < 0 || retTmp == 1) {
			return -1;
		}
		offsetTmp += Constant.POINT_PER_BIT;

		for (int i = 0; i < 8; i++) {
			retTmp = WaveDecoder.convertBit(data, offsetTmp);
			if (retTmp < 0) {
				return -1;
			}
			offsetTmp += Constant.POINT_PER_BIT;

			ret += (retTmp << (7 - i));
		}

		retTmp = WaveDecoder.convertBit(data, offsetTmp);
		if (retTmp <= 0) {
			return -1;
		}
		// offsetTmp += Constant.POINT_PER_SAMPLE;

		System.out.println("Decode succ:" + ret);
		return ret;
	}
}
