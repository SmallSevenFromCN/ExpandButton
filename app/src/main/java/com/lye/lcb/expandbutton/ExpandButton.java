package com.lye.lcb.expandbutton;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by liuchenbo on 2017/5/17.
 */

public class ExpandButton extends View {

    /**
     * 背景画笔
     */
    private Paint mBgPaint;
    /**
     * 圆的画笔
     */
    private Paint mCirclePaint;
    /**
     * 文字画笔
     */
    private Paint mTextPaint;
    /**
     * 图片画笔
     */
    private Paint mPicPaint;
    /**
     * 动画
     */
    private ValueAnimator animator;
    /**
     * 动画变动值，通过这个值来控制绘制图案的形状，达到通话效果
     */
    private float dValue;
    /**
     * 是否进行点击
     */
    private boolean isClick;
    /**
     * 判断是否按钮已经展开
     */
    private boolean isExpanded;
    /**
     * 是否已经开始动画了
     */
    private boolean isStartAnim;

    /**
     * 是否是从左边为起点
     */
    private boolean isLeft = true;

    /**
     * 第一个圆的圆心  X坐标
     */
    private float centreX = 150f;
    /**
     * 第一个圆的圆心  Y坐标
     */
    private float centreY = 50f;

    /**
     * 圆的半径
     */
    private float radius;
    /**
     * 圆形时候的颜色
     */
    private int circleColor;
    /**
     * 背景颜色
     */
    private int buttonBgColor;
    /**
     * 几个圆形的宽度  button按钮的宽度是以基本圆形的几个数量来计算的
     */
    private int circleCount;
    /**
     * 文字
     */
    private String buttonText;
    /**
     * 文字大小
     */
    private float textSize;
    /**
     * 文字颜色
     */
    private int textColor;
    /**
     * 图片路径
     */
    private int picSrc = R.mipmap.ic_launcher;
    /**
     * 动画时间间隔
     */
    private int animDuration;
    /**
     * 原始的图片
     */
    private Bitmap srcBitmap;
    /**
     * 缩小后的图片
     */
    private Bitmap desBitmap;

    /**
     * 矩阵，用来缩小图片
     */
    private Matrix mMatrix;


    public ExpandButton(Context context) {
        super(context);
    }

    public ExpandButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public ExpandButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs, defStyleAttr);
    }

    private void initAttrs(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ExpandButton, defStyleAttr, 0);
        int count = typedArray.getIndexCount();
        for (int i = 0; i < count; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.ExpandButton_radius){
                radius = typedArray.getDimension(index, radius);
            }else if (index == R.styleable.ExpandButton_buttonBgColor){
                buttonBgColor = typedArray.getColor(index, buttonBgColor);
            }else if (index == R.styleable.ExpandButton_circleCount){
                circleCount = typedArray.getInt(index, circleCount);
            }else if (index == R.styleable.ExpandButton_buttonText){
                buttonText = typedArray.getString(index);
            }else if (index == R.styleable.ExpandButton_buttonTextSize){
                textSize = typedArray.getDimension(index, textSize);
            }else if (index == R.styleable.ExpandButton_buttonTextColor){
                textColor = typedArray.getColor(index,textColor);
            }else if (index == R.styleable.ExpandButton_buttonPicSrc){
                picSrc = typedArray.getResourceId(index,R.mipmap.ic_launcher);
            }else if (index == R.styleable.ExpandButton_animDuration){
                animDuration = typedArray.getInt(index,300);
            }else if (index == R.styleable.ExpandButton_circleColor){
                circleColor = typedArray.getColor(index,circleColor);
            }else if (index == R.styleable.ExpandButton_isLeftSide){
                isLeft = typedArray.getBoolean(index,true);
            }
        }

        srcBitmap = BitmapFactory.decodeResource(context.getResources(), picSrc);
        init();
    }

    /**
     * 初始化
     */
    private void init() {

        centreX = (circleCount + 1) * radius;
        centreY = radius;
        isExpanded = isLeft;


        mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBgPaint.setStyle(Paint.Style.FILL);
        mBgPaint.setStrokeWidth(12);                     //设置圆边界宽度
        mBgPaint.setColor(buttonBgColor);

        mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCirclePaint.setStyle(Paint.Style.FILL);
        mCirclePaint.setStrokeWidth(12);                     //设置圆边界宽度
        mCirclePaint.setColor(circleColor);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setTextAlign(Paint.Align.LEFT);
        mTextPaint.setColor(textColor);

        mPicPaint = new Paint();
        mPicPaint.setAntiAlias(true);//抗锯齿,没有消除锯齿的话，图片变换会很难看的，因为有些像素点会失真


        /**
         * 使用多个数值，是增加差速器效果。是动画先慢后快
         */
        animator = ValueAnimator.ofFloat(1,0.6f,0.2f,0);
        /**
         * 动画的第一个监听，能返回具体变化的数值
         */
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (isExpanded){
                    dValue = 1 - (float) animation.getAnimatedValue();
                }else {
                    dValue = (float) animation.getAnimatedValue();
                }
                invalidate();
            }
        });

        /**
         * 动画的第二个监听，能监听动画的几个过程
         */
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {}

            @Override
            public void onAnimationEnd(Animator animation) {
                isExpanded = !isExpanded;
                isStartAnim = !isStartAnim;
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animator.setDuration(animDuration);
        mMatrix = new Matrix();
        getDesBitmap();

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int wSize = MeasureSpec.getSize(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        int hSize = MeasureSpec.getSize(heightMeasureSpec);
        /**
         * 测量宽度，这些在most和unspecified时候，直接计算为当前宽度
         */
        switch (wMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                //不超过父控件给的范围内，自由发挥
                int computeSize = (int) (getPaddingLeft() +  circleCount * radius+ 2 * radius  + getPaddingRight() );
                wSize = computeSize < wSize ? computeSize : wSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                //自由发挥
                computeSize = (int) (getPaddingLeft() + circleCount * radius + 2 * radius   + getPaddingRight());
                wSize = computeSize;
                break;
        }
        /**
         * 测量高度，这些在most和unspecified时候，直接计算为当前高度
         */
        switch (hMode) {
            case MeasureSpec.EXACTLY:
                break;
            case MeasureSpec.AT_MOST:
                int computeSize = (int) (getPaddingTop() + radius * 2 + getPaddingBottom() );
                hSize = computeSize < hSize ? computeSize : hSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                computeSize = (int) (getPaddingTop() + radius * 2 + getPaddingBottom());
                hSize = computeSize;
                break;
        }
        setMeasuredDimension(wSize, hSize);
    }




    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isLeft) {
            if (isClick){
                if ((centreX + radius)*dValue > 2*radius) {
                    RectF rectF = new RectF(0, 0, (centreX + radius) * dValue, centreY + radius);
                    canvas.drawRoundRect(rectF, radius, radius, mBgPaint);
                }else {
                    canvas.drawCircle(radius, centreY, radius, mCirclePaint);
                }

                Rect bound = new Rect();
                mTextPaint.getTextBounds(buttonText, 0, buttonText.length(), bound);
                /**
                 * 设置文字画笔的透明度，让文字出现的不是很突兀
                 */
                mTextPaint.setAlpha((int) (255 *  dValue));

                /**
                 * 让文字垂直居中绘制
                 */
                Paint.FontMetricsInt fontMetric = mTextPaint.getFontMetricsInt();
                int baseline = (int) ((centreY + radius - fontMetric.bottom + fontMetric.top) / 2 - fontMetric.top);
                /**
                 * 文字如果在第一个圆中，那它的left为centreX - radius - (2*radius-bound.width())/2
                 * 为了滚动到左边的时候，还能与左边有距离，就直接在变化数据外加上(2*radius-bound.width())/2
                 */
                canvas.drawText(buttonText, (centreX - radius) * dValue + (2 * radius - bound.width()) / 2, baseline, mTextPaint);

            } else {
                canvas.drawCircle(radius, centreY, radius, mCirclePaint);
            }

            /**
             * 绘制图片，注意坐标转换
             */
            canvas.drawBitmap(desBitmap, radius - desBitmap.getHeight() / 2, centreY - desBitmap.getHeight() / 2, mPicPaint);

        } else {

            if (isClick) {
                RectF rectF = new RectF((centreX - radius) * dValue, centreY - radius, centreX + radius, centreY + radius);
                /**
                 * rx x方向上的圆角半径
                 * ry y方向上的圆角半径
                 */
                canvas.drawRoundRect(rectF, radius, radius, mBgPaint);

                Rect bound = new Rect();
                mTextPaint.getTextBounds(buttonText, 0, buttonText.length(), bound);
                /**
                 * 设置文字画笔的透明度，让文字出现的不是很突兀
                 */
                mTextPaint.setAlpha((int) (255 * (1 - dValue)));

                /**
                 * 让文字垂直居中绘制
                 */
                Paint.FontMetricsInt fontMetric = mTextPaint.getFontMetricsInt();
                int baseline = (int) ((centreY + radius - fontMetric.bottom + fontMetric.top) / 2 - fontMetric.top);
                /**
                 * 文字如果在第一个圆中，那它的left为centreX - radius - (2*radius-bound.width())/2
                 * 为了滚动到左边的时候，还能与左边有距离，就直接在变化数据外加上(2*radius-bound.width())/2
                 */
                canvas.drawText(buttonText, (centreX - radius) * dValue + (2 * radius - bound.width()) / 2, baseline, mTextPaint);
                /**
                 * 控制在展开和关闭的时候颜色的切换
                 */
                if (!isExpanded && !isStartAnim) {
                    canvas.drawCircle(centreX, centreY, radius, mCirclePaint);
                }

            } else {
                /**
                 * 绘制第一个圆形
                 */
                canvas.drawCircle(centreX, centreY, radius, mCirclePaint);
            }


            /**
             * 绘制图片，注意坐标转换
             */
            canvas.drawBitmap(desBitmap, centreX - desBitmap.getHeight() / 2, centreY - desBitmap.getHeight() / 2, mPicPaint);

        }


    }



    private void getDesBitmap() {
        /**
         * 初始化图片与view之间伸缩比例，因为比例一般非整数，所以用float，免得精度丢失
         */
        float scale = 1.0f;

        /**
         * 将图片的宽度高度的最小者作为图片的边长，用来和view来计算伸缩比例
         */
        int bitmapSize = Math.min(srcBitmap.getHeight(), srcBitmap.getWidth());
        /**
         * 计算缩放比例，view的大小和图片的大小比例
         */
        scale = 2 * radius * 1.0f / bitmapSize;
        /**
         * 利用这个图像变换处理器，设置伸缩比例，长宽以相同比例伸缩
         */
        mMatrix.setScale(scale, scale);
        desBitmap = Bitmap.createBitmap(this.srcBitmap, 0, 0, srcBitmap.getWidth(), srcBitmap.getHeight(), mMatrix, true);
    }





    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                /**
                 * 之所以要进行这个操作，是为了拦截在未展开的时候，点击空白区域的事件。
                 * 使点击空白区域不会触发OnClickListener
                 */
                if (findPointAt(event)){
                   return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 找到点击的位置，是否在已经绘制的区域内，如果在就开始动画
     * @param event
     */
    private boolean findPointAt(MotionEvent event){
        float x = event.getX();
        float y = event.getY();
        Region r = new Region();
        if (isLeft){
            if (isExpanded) {
                r.set(0, 0, (int) (2 * radius), (int) (2 * radius));
            } else {
                r.set(0, 0, (int) (centreX + radius), (int) (centreY + radius));
            }

        }else {
            if (isExpanded) {
                r.set(0, 0, (int) (centreX + radius), (int) (centreY + radius));
            } else {
                r.set((int) (centreX - radius), (int) (centreY - radius), (int) (centreX + radius), (int) (centreY + radius));
            }
        }

        if (r.contains((int)x,(int)y)){
            startAnimator();
            if (null != listener){
                listener.onClick();
            }
            return true;
        }
        return false;
    }

    /**
     * 开始动画，只为了把animator.start()和isStartAnim放到一个方法
     */
    private void startAnimator(){
        animator.start();
        isStartAnim = true;
        isClick = true;
    }
    /**
     * 获取按钮当前的状态
     * @return
     */
    public boolean getIsExpand(){
        boolean bol;
        /**
         * 左右的展开状态不一致，成相反关系，具体为啥这样子，我也没理清。。哈哈
         */
        if (isLeft){
            bol = !isExpanded;
        } else {
            bol = isExpanded;
        }
        return bol;
    }

    /**
     * 展开按钮
     */
    public void setButtonExpanded(){
        if (!isExpanded){
            startAnimator();
        }
    }


    /**
     * 恢复按钮
     */
    public void setButtonRecover(){
        if (isExpanded){
            startAnimator();
        }
    }

    /**
     * 切换按钮的状态
     */
    public void changeButtonStatus(){
        startAnimator();
    }


    /**
     * 点击响应接口
     */
    private OnClickExpandButtonListener listener;

    public void setOnClickExpandButtonListener(OnClickExpandButtonListener listener){
        this.listener = listener;
    }
    public interface OnClickExpandButtonListener{
        void onClick();
    }

}
