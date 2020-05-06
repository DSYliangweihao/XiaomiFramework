package android.widget;

import android.util.SparseArray;
import android.view.inspector.InspectionCompanion;
import android.view.inspector.InspectionCompanion.UninitializedPropertyMapException;
import android.view.inspector.IntFlagMapping;
import android.view.inspector.PropertyMapper;
import android.view.inspector.PropertyReader;
import java.util.Objects;

public final class TextView$InspectionCompanion
  implements InspectionCompanion<TextView>
{
  private int mAutoLinkId;
  private int mAutoSizeMaxTextSizeId;
  private int mAutoSizeMinTextSizeId;
  private int mAutoSizeStepGranularityId;
  private int mAutoSizeTextTypeId;
  private int mBreakStrategyId;
  private int mCursorVisibleId;
  private int mDrawableBlendModeId;
  private int mDrawablePaddingId;
  private int mDrawableTintId;
  private int mDrawableTintModeId;
  private int mElegantTextHeightId;
  private int mEllipsizeId;
  private int mFallbackLineSpacingId;
  private int mFirstBaselineToTopHeightId;
  private int mFontFeatureSettingsId;
  private int mFreezesTextId;
  private int mGravityId;
  private int mHintId;
  private int mHyphenationFrequencyId;
  private int mImeActionIdId;
  private int mImeActionLabelId;
  private int mImeOptionsId;
  private int mIncludeFontPaddingId;
  private int mInputTypeId;
  private int mJustificationModeId;
  private int mLastBaselineToBottomHeightId;
  private int mLetterSpacingId;
  private int mLineHeightId;
  private int mLineSpacingExtraId;
  private int mLineSpacingMultiplierId;
  private int mLinksClickableId;
  private int mMarqueeRepeatLimitId;
  private int mMaxEmsId;
  private int mMaxHeightId;
  private int mMaxLinesId;
  private int mMaxWidthId;
  private int mMinEmsId;
  private int mMinLinesId;
  private int mMinWidthId;
  private int mPrivateImeOptionsId;
  private boolean mPropertiesMapped = false;
  private int mScrollHorizontallyId;
  private int mShadowColorId;
  private int mShadowDxId;
  private int mShadowDyId;
  private int mShadowRadiusId;
  private int mSingleLineId;
  private int mTextAllCapsId;
  private int mTextColorHighlightId;
  private int mTextColorHintId;
  private int mTextColorId;
  private int mTextColorLinkId;
  private int mTextId;
  private int mTextIsSelectableId;
  private int mTextScaleXId;
  private int mTextSizeId;
  private int mTypefaceId;
  
  public void mapProperties(PropertyMapper paramPropertyMapper)
  {
    Object localObject = new IntFlagMapping();
    ((IntFlagMapping)localObject).add(2, 2, "email");
    ((IntFlagMapping)localObject).add(8, 8, "map");
    ((IntFlagMapping)localObject).add(4, 4, "phone");
    ((IntFlagMapping)localObject).add(1, 1, "web");
    Objects.requireNonNull(localObject);
    this.mAutoLinkId = paramPropertyMapper.mapIntFlag("autoLink", 16842928, new _..Lambda.gFNlJIKfxqleu304aRWP5R5v1yY((IntFlagMapping)localObject));
    this.mAutoSizeMaxTextSizeId = paramPropertyMapper.mapInt("autoSizeMaxTextSize", 16844102);
    this.mAutoSizeMinTextSizeId = paramPropertyMapper.mapInt("autoSizeMinTextSize", 16844088);
    this.mAutoSizeStepGranularityId = paramPropertyMapper.mapInt("autoSizeStepGranularity", 16844086);
    localObject = new SparseArray();
    ((SparseArray)localObject).put(0, "none");
    ((SparseArray)localObject).put(1, "uniform");
    Objects.requireNonNull(localObject);
    this.mAutoSizeTextTypeId = paramPropertyMapper.mapIntEnum("autoSizeTextType", 16844085, new _..Lambda.QY3N4tzLteuFdjRnyJFCbR1ajSI((SparseArray)localObject));
    localObject = new SparseArray();
    ((SparseArray)localObject).put(0, "simple");
    ((SparseArray)localObject).put(1, "high_quality");
    ((SparseArray)localObject).put(2, "balanced");
    Objects.requireNonNull(localObject);
    this.mBreakStrategyId = paramPropertyMapper.mapIntEnum("breakStrategy", 16843997, new _..Lambda.QY3N4tzLteuFdjRnyJFCbR1ajSI((SparseArray)localObject));
    this.mCursorVisibleId = paramPropertyMapper.mapBoolean("cursorVisible", 16843090);
    this.mDrawableBlendModeId = paramPropertyMapper.mapObject("drawableBlendMode", 80);
    this.mDrawablePaddingId = paramPropertyMapper.mapInt("drawablePadding", 16843121);
    this.mDrawableTintId = paramPropertyMapper.mapObject("drawableTint", 16843990);
    this.mDrawableTintModeId = paramPropertyMapper.mapObject("drawableTintMode", 16843991);
    this.mElegantTextHeightId = paramPropertyMapper.mapBoolean("elegantTextHeight", 16843869);
    this.mEllipsizeId = paramPropertyMapper.mapObject("ellipsize", 16842923);
    this.mFallbackLineSpacingId = paramPropertyMapper.mapBoolean("fallbackLineSpacing", 16844155);
    this.mFirstBaselineToTopHeightId = paramPropertyMapper.mapInt("firstBaselineToTopHeight", 16844157);
    this.mFontFeatureSettingsId = paramPropertyMapper.mapObject("fontFeatureSettings", 16843959);
    this.mFreezesTextId = paramPropertyMapper.mapBoolean("freezesText", 16843116);
    this.mGravityId = paramPropertyMapper.mapGravity("gravity", 16842927);
    this.mHintId = paramPropertyMapper.mapObject("hint", 16843088);
    localObject = new SparseArray();
    ((SparseArray)localObject).put(0, "none");
    ((SparseArray)localObject).put(1, "normal");
    ((SparseArray)localObject).put(2, "full");
    Objects.requireNonNull(localObject);
    this.mHyphenationFrequencyId = paramPropertyMapper.mapIntEnum("hyphenationFrequency", 16843998, new _..Lambda.QY3N4tzLteuFdjRnyJFCbR1ajSI((SparseArray)localObject));
    this.mImeActionIdId = paramPropertyMapper.mapInt("imeActionId", 16843366);
    this.mImeActionLabelId = paramPropertyMapper.mapObject("imeActionLabel", 16843365);
    localObject = new IntFlagMapping();
    ((IntFlagMapping)localObject).add(255, 6, "actionDone");
    ((IntFlagMapping)localObject).add(255, 2, "actionGo");
    ((IntFlagMapping)localObject).add(255, 5, "actionNext");
    ((IntFlagMapping)localObject).add(255, 1, "actionNone");
    ((IntFlagMapping)localObject).add(255, 7, "actionPrevious");
    ((IntFlagMapping)localObject).add(255, 3, "actionSearch");
    ((IntFlagMapping)localObject).add(255, 4, "actionSend");
    ((IntFlagMapping)localObject).add(255, 0, "actionUnspecified");
    ((IntFlagMapping)localObject).add(Integer.MIN_VALUE, Integer.MIN_VALUE, "flagForceAscii");
    ((IntFlagMapping)localObject).add(134217728, 134217728, "flagNavigateNext");
    ((IntFlagMapping)localObject).add(67108864, 67108864, "flagNavigatePrevious");
    ((IntFlagMapping)localObject).add(536870912, 536870912, "flagNoAccessoryAction");
    ((IntFlagMapping)localObject).add(1073741824, 1073741824, "flagNoEnterAction");
    ((IntFlagMapping)localObject).add(268435456, 268435456, "flagNoExtractUi");
    ((IntFlagMapping)localObject).add(33554432, 33554432, "flagNoFullscreen");
    ((IntFlagMapping)localObject).add(16777216, 16777216, "flagNoPersonalizedLearning");
    ((IntFlagMapping)localObject).add(-1, 0, "normal");
    Objects.requireNonNull(localObject);
    this.mImeOptionsId = paramPropertyMapper.mapIntFlag("imeOptions", 16843364, new _..Lambda.gFNlJIKfxqleu304aRWP5R5v1yY((IntFlagMapping)localObject));
    this.mIncludeFontPaddingId = paramPropertyMapper.mapBoolean("includeFontPadding", 16843103);
    localObject = new IntFlagMapping();
    ((IntFlagMapping)localObject).add(4095, 20, "date");
    ((IntFlagMapping)localObject).add(4095, 4, "datetime");
    ((IntFlagMapping)localObject).add(-1, 0, "none");
    ((IntFlagMapping)localObject).add(4095, 2, "number");
    ((IntFlagMapping)localObject).add(16773135, 8194, "numberDecimal");
    ((IntFlagMapping)localObject).add(4095, 18, "numberPassword");
    ((IntFlagMapping)localObject).add(16773135, 4098, "numberSigned");
    ((IntFlagMapping)localObject).add(4095, 3, "phone");
    ((IntFlagMapping)localObject).add(4095, 1, "text");
    ((IntFlagMapping)localObject).add(16773135, 65537, "textAutoComplete");
    ((IntFlagMapping)localObject).add(16773135, 32769, "textAutoCorrect");
    ((IntFlagMapping)localObject).add(16773135, 4097, "textCapCharacters");
    ((IntFlagMapping)localObject).add(16773135, 16385, "textCapSentences");
    ((IntFlagMapping)localObject).add(16773135, 8193, "textCapWords");
    ((IntFlagMapping)localObject).add(4095, 33, "textEmailAddress");
    ((IntFlagMapping)localObject).add(4095, 49, "textEmailSubject");
    ((IntFlagMapping)localObject).add(4095, 177, "textFilter");
    ((IntFlagMapping)localObject).add(16773135, 262145, "textImeMultiLine");
    ((IntFlagMapping)localObject).add(4095, 81, "textLongMessage");
    ((IntFlagMapping)localObject).add(16773135, 131073, "textMultiLine");
    ((IntFlagMapping)localObject).add(16773135, 524289, "textNoSuggestions");
    ((IntFlagMapping)localObject).add(4095, 129, "textPassword");
    ((IntFlagMapping)localObject).add(4095, 97, "textPersonName");
    ((IntFlagMapping)localObject).add(4095, 193, "textPhonetic");
    ((IntFlagMapping)localObject).add(4095, 113, "textPostalAddress");
    ((IntFlagMapping)localObject).add(4095, 65, "textShortMessage");
    ((IntFlagMapping)localObject).add(4095, 17, "textUri");
    ((IntFlagMapping)localObject).add(4095, 145, "textVisiblePassword");
    ((IntFlagMapping)localObject).add(4095, 161, "textWebEditText");
    ((IntFlagMapping)localObject).add(4095, 209, "textWebEmailAddress");
    ((IntFlagMapping)localObject).add(4095, 225, "textWebPassword");
    ((IntFlagMapping)localObject).add(4095, 36, "time");
    Objects.requireNonNull(localObject);
    this.mInputTypeId = paramPropertyMapper.mapIntFlag("inputType", 16843296, new _..Lambda.gFNlJIKfxqleu304aRWP5R5v1yY((IntFlagMapping)localObject));
    localObject = new SparseArray();
    ((SparseArray)localObject).put(0, "none");
    ((SparseArray)localObject).put(1, "inter_word");
    Objects.requireNonNull(localObject);
    this.mJustificationModeId = paramPropertyMapper.mapIntEnum("justificationMode", 16844135, new _..Lambda.QY3N4tzLteuFdjRnyJFCbR1ajSI((SparseArray)localObject));
    this.mLastBaselineToBottomHeightId = paramPropertyMapper.mapInt("lastBaselineToBottomHeight", 16844158);
    this.mLetterSpacingId = paramPropertyMapper.mapFloat("letterSpacing", 16843958);
    this.mLineHeightId = paramPropertyMapper.mapInt("lineHeight", 16844159);
    this.mLineSpacingExtraId = paramPropertyMapper.mapFloat("lineSpacingExtra", 16843287);
    this.mLineSpacingMultiplierId = paramPropertyMapper.mapFloat("lineSpacingMultiplier", 16843288);
    this.mLinksClickableId = paramPropertyMapper.mapBoolean("linksClickable", 16842929);
    this.mMarqueeRepeatLimitId = paramPropertyMapper.mapInt("marqueeRepeatLimit", 16843293);
    this.mMaxEmsId = paramPropertyMapper.mapInt("maxEms", 16843095);
    this.mMaxHeightId = paramPropertyMapper.mapInt("maxHeight", 16843040);
    this.mMaxLinesId = paramPropertyMapper.mapInt("maxLines", 16843091);
    this.mMaxWidthId = paramPropertyMapper.mapInt("maxWidth", 16843039);
    this.mMinEmsId = paramPropertyMapper.mapInt("minEms", 16843098);
    this.mMinLinesId = paramPropertyMapper.mapInt("minLines", 16843094);
    this.mMinWidthId = paramPropertyMapper.mapInt("minWidth", 16843071);
    this.mPrivateImeOptionsId = paramPropertyMapper.mapObject("privateImeOptions", 16843299);
    this.mScrollHorizontallyId = paramPropertyMapper.mapBoolean("scrollHorizontally", 16843099);
    this.mShadowColorId = paramPropertyMapper.mapColor("shadowColor", 16843105);
    this.mShadowDxId = paramPropertyMapper.mapFloat("shadowDx", 16843106);
    this.mShadowDyId = paramPropertyMapper.mapFloat("shadowDy", 16843107);
    this.mShadowRadiusId = paramPropertyMapper.mapFloat("shadowRadius", 16843108);
    this.mSingleLineId = paramPropertyMapper.mapBoolean("singleLine", 16843101);
    this.mTextId = paramPropertyMapper.mapObject("text", 16843087);
    this.mTextAllCapsId = paramPropertyMapper.mapBoolean("textAllCaps", 16843660);
    this.mTextColorId = paramPropertyMapper.mapObject("textColor", 16842904);
    this.mTextColorHighlightId = paramPropertyMapper.mapColor("textColorHighlight", 16842905);
    this.mTextColorHintId = paramPropertyMapper.mapObject("textColorHint", 16842906);
    this.mTextColorLinkId = paramPropertyMapper.mapObject("textColorLink", 16842907);
    this.mTextIsSelectableId = paramPropertyMapper.mapBoolean("textIsSelectable", 16843542);
    this.mTextScaleXId = paramPropertyMapper.mapFloat("textScaleX", 16843089);
    this.mTextSizeId = paramPropertyMapper.mapFloat("textSize", 16842901);
    this.mTypefaceId = paramPropertyMapper.mapObject("typeface", 16842902);
    this.mPropertiesMapped = true;
  }
  
  public void readProperties(TextView paramTextView, PropertyReader paramPropertyReader)
  {
    if (this.mPropertiesMapped)
    {
      paramPropertyReader.readIntFlag(this.mAutoLinkId, paramTextView.getAutoLinkMask());
      paramPropertyReader.readInt(this.mAutoSizeMaxTextSizeId, paramTextView.getAutoSizeMaxTextSize());
      paramPropertyReader.readInt(this.mAutoSizeMinTextSizeId, paramTextView.getAutoSizeMinTextSize());
      paramPropertyReader.readInt(this.mAutoSizeStepGranularityId, paramTextView.getAutoSizeStepGranularity());
      paramPropertyReader.readIntEnum(this.mAutoSizeTextTypeId, paramTextView.getAutoSizeTextType());
      paramPropertyReader.readIntEnum(this.mBreakStrategyId, paramTextView.getBreakStrategy());
      paramPropertyReader.readBoolean(this.mCursorVisibleId, paramTextView.isCursorVisible());
      paramPropertyReader.readObject(this.mDrawableBlendModeId, paramTextView.getCompoundDrawableTintBlendMode());
      paramPropertyReader.readInt(this.mDrawablePaddingId, paramTextView.getCompoundDrawablePadding());
      paramPropertyReader.readObject(this.mDrawableTintId, paramTextView.getCompoundDrawableTintList());
      paramPropertyReader.readObject(this.mDrawableTintModeId, paramTextView.getCompoundDrawableTintMode());
      paramPropertyReader.readBoolean(this.mElegantTextHeightId, paramTextView.isElegantTextHeight());
      paramPropertyReader.readObject(this.mEllipsizeId, paramTextView.getEllipsize());
      paramPropertyReader.readBoolean(this.mFallbackLineSpacingId, paramTextView.isFallbackLineSpacing());
      paramPropertyReader.readInt(this.mFirstBaselineToTopHeightId, paramTextView.getFirstBaselineToTopHeight());
      paramPropertyReader.readObject(this.mFontFeatureSettingsId, paramTextView.getFontFeatureSettings());
      paramPropertyReader.readBoolean(this.mFreezesTextId, paramTextView.getFreezesText());
      paramPropertyReader.readGravity(this.mGravityId, paramTextView.getGravity());
      paramPropertyReader.readObject(this.mHintId, paramTextView.getHint());
      paramPropertyReader.readIntEnum(this.mHyphenationFrequencyId, paramTextView.getHyphenationFrequency());
      paramPropertyReader.readInt(this.mImeActionIdId, paramTextView.getImeActionId());
      paramPropertyReader.readObject(this.mImeActionLabelId, paramTextView.getImeActionLabel());
      paramPropertyReader.readIntFlag(this.mImeOptionsId, paramTextView.getImeOptions());
      paramPropertyReader.readBoolean(this.mIncludeFontPaddingId, paramTextView.getIncludeFontPadding());
      paramPropertyReader.readIntFlag(this.mInputTypeId, paramTextView.getInputType());
      paramPropertyReader.readIntEnum(this.mJustificationModeId, paramTextView.getJustificationMode());
      paramPropertyReader.readInt(this.mLastBaselineToBottomHeightId, paramTextView.getLastBaselineToBottomHeight());
      paramPropertyReader.readFloat(this.mLetterSpacingId, paramTextView.getLetterSpacing());
      paramPropertyReader.readInt(this.mLineHeightId, paramTextView.getLineHeight());
      paramPropertyReader.readFloat(this.mLineSpacingExtraId, paramTextView.getLineSpacingExtra());
      paramPropertyReader.readFloat(this.mLineSpacingMultiplierId, paramTextView.getLineSpacingMultiplier());
      paramPropertyReader.readBoolean(this.mLinksClickableId, paramTextView.getLinksClickable());
      paramPropertyReader.readInt(this.mMarqueeRepeatLimitId, paramTextView.getMarqueeRepeatLimit());
      paramPropertyReader.readInt(this.mMaxEmsId, paramTextView.getMaxEms());
      paramPropertyReader.readInt(this.mMaxHeightId, paramTextView.getMaxHeight());
      paramPropertyReader.readInt(this.mMaxLinesId, paramTextView.getMaxLines());
      paramPropertyReader.readInt(this.mMaxWidthId, paramTextView.getMaxWidth());
      paramPropertyReader.readInt(this.mMinEmsId, paramTextView.getMinEms());
      paramPropertyReader.readInt(this.mMinLinesId, paramTextView.getMinLines());
      paramPropertyReader.readInt(this.mMinWidthId, paramTextView.getMinWidth());
      paramPropertyReader.readObject(this.mPrivateImeOptionsId, paramTextView.getPrivateImeOptions());
      paramPropertyReader.readBoolean(this.mScrollHorizontallyId, paramTextView.isHorizontallyScrollable());
      paramPropertyReader.readColor(this.mShadowColorId, paramTextView.getShadowColor());
      paramPropertyReader.readFloat(this.mShadowDxId, paramTextView.getShadowDx());
      paramPropertyReader.readFloat(this.mShadowDyId, paramTextView.getShadowDy());
      paramPropertyReader.readFloat(this.mShadowRadiusId, paramTextView.getShadowRadius());
      paramPropertyReader.readBoolean(this.mSingleLineId, paramTextView.isSingleLine());
      paramPropertyReader.readObject(this.mTextId, paramTextView.getText());
      paramPropertyReader.readBoolean(this.mTextAllCapsId, paramTextView.isAllCaps());
      paramPropertyReader.readObject(this.mTextColorId, paramTextView.getTextColors());
      paramPropertyReader.readColor(this.mTextColorHighlightId, paramTextView.getHighlightColor());
      paramPropertyReader.readObject(this.mTextColorHintId, paramTextView.getHintTextColors());
      paramPropertyReader.readObject(this.mTextColorLinkId, paramTextView.getLinkTextColors());
      paramPropertyReader.readBoolean(this.mTextIsSelectableId, paramTextView.isTextSelectable());
      paramPropertyReader.readFloat(this.mTextScaleXId, paramTextView.getTextScaleX());
      paramPropertyReader.readFloat(this.mTextSizeId, paramTextView.getTextSize());
      paramPropertyReader.readObject(this.mTypefaceId, paramTextView.getTypeface());
      return;
    }
    throw new InspectionCompanion.UninitializedPropertyMapException();
  }
}


/* Location:              /Users/sanbo/Desktop/framework/miui/framework/classes3-dex2jar.jar!/android/widget/TextView$InspectionCompanion.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */