/*
* Copyright (C) 2015 Vincent Mi
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.hugbio.view;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import com.makeramen.roundedimageview.Corner;
import com.makeramen.roundedimageview.RoundedDrawable;

import net.tsz.afinal.bitmap.core.BitmapDownloadCallback;

public final class RoundedLoadCallbackBuilder {

  private final DisplayMetrics mDisplayMetrics;

  private float[] mCornerRadii = new float[] { 0, 0, 0, 0 };

  private boolean mOval = false;
  private float mBorderWidth = 0;
  private ColorStateList mBorderColor =
      ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
  private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

  public RoundedLoadCallbackBuilder() {
    mDisplayMetrics = Resources.getSystem().getDisplayMetrics();
  }

  public RoundedLoadCallbackBuilder scaleType(ImageView.ScaleType scaleType) {
    mScaleType = scaleType;
    return this;
  }

  /**
   * 为四个角设置圆角半径。单位px
   */
  public RoundedLoadCallbackBuilder cornerRadius(float radius) {
    mCornerRadii[Corner.TOP_LEFT] = radius;
    mCornerRadii[Corner.TOP_RIGHT] = radius;
    mCornerRadii[Corner.BOTTOM_RIGHT] = radius;
    mCornerRadii[Corner.BOTTOM_LEFT] = radius;
    return this;
  }

  /**
   * 为单个角设置圆角半径。单位px
   */
  public RoundedLoadCallbackBuilder cornerRadius(int corner, float radius) {
    mCornerRadii[corner] = radius;
    return this;
  }

  /**
   * 为四个角设置圆角半径。单位dp
   */
  public RoundedLoadCallbackBuilder cornerRadiusDp(float radius) {
    return cornerRadius(
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, mDisplayMetrics));
  }

  /**
   * 为单个角设置圆角半径。单位dp
   */
  public RoundedLoadCallbackBuilder cornerRadiusDp(int corner, float radius) {
    return cornerRadius(corner,
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, mDisplayMetrics));
  }

  /**
   * 设置边框宽度。单位px
   */
  public RoundedLoadCallbackBuilder borderWidth(float width) {
    mBorderWidth = width;
    return this;
  }

  /**
   * 设置边框宽度。单位dp
   */
  public RoundedLoadCallbackBuilder borderWidthDp(float width) {
    mBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, mDisplayMetrics);
    return this;
  }

  /**
   * 设置边框颜色
   */
  public RoundedLoadCallbackBuilder borderColor(int color) {
    mBorderColor = ColorStateList.valueOf(color);
    return this;
  }

  /**
   * Set the border color as a {@link ColorStateList}.
   *
   * @param colors the {@link ColorStateList} to set.
   * @return the builder for chaining.
   */
  public RoundedLoadCallbackBuilder borderColor(ColorStateList colors) {
    mBorderColor = colors;
    return this;
  }

  /**
   * 设置是否为圆形
   */
  public RoundedLoadCallbackBuilder oval(boolean oval) {
    mOval = oval;
    return this;
  }

  /**
   * Creates a {@link BitmapDownloadCallback} for use with LoadBitmap.
   *
   * @return the {@link BitmapDownloadCallback}
   */
  public BitmapDownloadCallback build() {
    return new BitmapDownloadCallback() {
      @Override
      public Bitmap onDownloadSuccessForProcess(Bitmap source) {
        Bitmap transformed = RoundedDrawable.fromBitmap(source)
                .setScaleType(mScaleType)
                .setCornerRadius(mCornerRadii[0], mCornerRadii[1], mCornerRadii[2], mCornerRadii[3])
                .setBorderWidth(mBorderWidth)
                .setBorderColor(mBorderColor)
                .setOval(mOval)
                .toBitmap();
        return transformed;
      }

      @Override
      public void onBitmapDownloadPrepare() {
      }

      @Override
      public void onBitmapDownloadLoading(int total, int percent) {
      }

      @Override
      public void onBitmapDownloadSuccess() {
      }

      @Override
      public void onBitmapDownloadFail() {
      }
//      @Override
//      public String key() {
//        return "r:" + Arrays.toString(mCornerRadii)
//            + "b:" + mBorderWidth
//            + "c:" + mBorderColor
//            + "o:" + mOval;
//      }
    };
  }
}
