package android.widget;

import android.content.Context;
import android.graphics.RenderNode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.PointerIcon;

@RemoteViews.RemoteView
public class ImageButton
  extends ImageView
{
  public ImageButton(Context paramContext)
  {
    this(paramContext, null);
  }
  
  public ImageButton(Context paramContext, AttributeSet paramAttributeSet)
  {
    this(paramContext, paramAttributeSet, 16842866);
  }
  
  public ImageButton(Context paramContext, AttributeSet paramAttributeSet, int paramInt)
  {
    this(paramContext, paramAttributeSet, paramInt, 0);
  }
  
  public ImageButton(Context paramContext, AttributeSet paramAttributeSet, int paramInt1, int paramInt2)
  {
    super(paramContext, paramAttributeSet, paramInt1, paramInt2);
    setFocusable(true);
    getRenderNode().setUsageHint(2);
  }
  
  public CharSequence getAccessibilityClassName()
  {
    return ImageButton.class.getName();
  }
  
  public PointerIcon onResolvePointerIcon(MotionEvent paramMotionEvent, int paramInt)
  {
    if ((getPointerIcon() == null) && (isClickable()) && (isEnabled())) {
      return PointerIcon.getSystemIcon(getContext(), 1002);
    }
    return super.onResolvePointerIcon(paramMotionEvent, paramInt);
  }
  
  protected boolean onSetAlpha(int paramInt)
  {
    return false;
  }
}


/* Location:              /Users/sanbo/Desktop/framework/miui/framework/classes3-dex2jar.jar!/android/widget/ImageButton.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       0.7.1
 */