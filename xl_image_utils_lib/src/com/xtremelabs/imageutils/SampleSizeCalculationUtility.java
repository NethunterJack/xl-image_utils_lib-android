package com.xtremelabs.imageutils;

import android.util.FloatMath;

import com.xtremelabs.imageutils.AbstractImageLoader.Options;
import com.xtremelabs.imageutils.AbstractImageLoader.Options.ScalingPreference;

class SampleSizeCalculationUtility {
	/**
	 * Calculates a sample size that will potentially save memory and not result in a loss of quality when the image is made to fill the image view.
	 * 
	 * @param width
	 *            The image will not be scaled down to be smaller than this width. Null for no scaling by width.
	 * @param height
	 *            The image will not be scaled down to be smaller than this height. Null for no scaling by height.
	 * @param imageDimensions
	 *            The dimensions of the image, as decoded from the full image on disk.
	 * @return The calculated sample size. 1 if both height and width are null.
	 */
	public static int calculateSampleSize(ImageRequest imageRequest, Dimensions imageDimensions) {
		ScalingInfo scalingInfo = imageRequest.getScalingInfo();
		Options options = imageRequest.getOptions();
		ScalingPreference scalingPreference = ScalingPreference.SMALLER_THAN_VIEW;
		if (options != null && options.scalingPreference != null) {
			scalingPreference = options.scalingPreference;
		}

		final Integer width = scalingInfo.width;
		final Integer height = scalingInfo.height;
		final int imageWidth = imageDimensions.getWidth();
		final int imageHeight = imageDimensions.getHeight();

		int widthSampleSize;
		int heightSampleSize;

		widthSampleSize = calculateSampleSizeForDimension(imageWidth, width, scalingPreference);
		heightSampleSize = calculateSampleSizeForDimension(imageHeight, height, scalingPreference);
		return calculateOverallSampleSize(widthSampleSize, heightSampleSize, scalingPreference);
	}

	private static int calculateOverallSampleSize(int widthSampleSize, int heightSampleSize, ScalingPreference scalingPreference) {
		int sampleSize = 1;

		if (widthSampleSize != -1 && heightSampleSize != -1) {
			switch (scalingPreference) {
			case MATCH_TO_LARGER_DIMENSION:
			case LARGER_THAN_VIEW_OR_FULL_SIZE:
				sampleSize = Math.min(widthSampleSize, heightSampleSize);
				break;
			case ROUND_TO_CLOSEST_MATCH:
			case MATCH_TO_SMALLER_DIMENSION:
			case SMALLER_THAN_VIEW:
				sampleSize = Math.max(widthSampleSize, heightSampleSize);
				break;
			}
		} else if (widthSampleSize != -1 || heightSampleSize != -1) {
			int tempSampleSize;
			if (widthSampleSize == -1) {
				tempSampleSize = heightSampleSize;
			} else {
				tempSampleSize = widthSampleSize;
			}

			switch (scalingPreference) {
			case MATCH_TO_LARGER_DIMENSION:
			case MATCH_TO_SMALLER_DIMENSION:
			case ROUND_TO_CLOSEST_MATCH:
			case SMALLER_THAN_VIEW:
				sampleSize = tempSampleSize;
				break;
			case LARGER_THAN_VIEW_OR_FULL_SIZE:
				break;
			}
		}

		return sampleSize;
	}

	private static int calculateSampleSizeForDimension(int imageDimension, Integer boundingDimension, ScalingPreference scalingPreference) {
		int sampleSize = 1;

		if (boundingDimension == null) {
			sampleSize = -1;
		} else if (imageDimension <= boundingDimension) {
			sampleSize = 1;
		} else {
			float imageWidthToBoundsWidthRatio = (float) imageDimension / (float) boundingDimension;
			switch (scalingPreference) {
			case MATCH_TO_SMALLER_DIMENSION:
			case MATCH_TO_LARGER_DIMENSION:
			case LARGER_THAN_VIEW_OR_FULL_SIZE:
				sampleSize = (int) imageWidthToBoundsWidthRatio;
				break;
			case ROUND_TO_CLOSEST_MATCH:
				sampleSize = Math.round(imageWidthToBoundsWidthRatio);
				break;
			case SMALLER_THAN_VIEW:
				sampleSize = (int) FloatMath.ceil(imageWidthToBoundsWidthRatio);
				break;
			}
		}

		return sampleSize;
	}
}
