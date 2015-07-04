package com.gamelift.demo;

import android.content.Context;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.drawable.PaintDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * Created by Deepscorn on 04/07/15. For questions and licensing contact vnms11@gmail.com
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static class CardHolder extends RecyclerView.ViewHolder {
        CardHolder(View root) {
            super(root);
        }

        static void createGradient(ImageView image, final int color) {
            final int height = image.getHeight();
            ShapeDrawable.ShaderFactory shaderFactory = new ShapeDrawable.ShaderFactory() {
                @Override
                public Shader resize(int width, int height) {
                    LinearGradient gradient = new LinearGradient(0, 0, 0, height,
                            new int[] {
                                    (color & 0x00ffffff) | 0xbf000000, //0.75f
                                    (color & 0x00ffffff) | 0x66000000, //0.4f
                                    (color & 0x00ffffff) | 0xcc000000, //0.8f
                            },
                            new float[] {
                                    0, 0.55f, 1
                            },
                            Shader.TileMode.REPEAT);
                    return gradient;
                }
            };
            PaintDrawable paintDrawable = new PaintDrawable();
            paintDrawable.setShape(new RectShape());
            paintDrawable.setShaderFactory(shaderFactory);
            image.setBackgroundDrawable(paintDrawable);
        }
    }

    private final int expandedHeight;
    private final int collapsedHeight;
    private final View.OnTouchListener onTouchListener;
    private int[][] cardColoursArray = {
            { 0x00aaff, 0xbbcc00 },
            { 0xff00ff, 0x0000ee },
            { 0xffaaff, 0xbbccee },
            { 0x001f54, 0x6fff80 },
            { 0xf13f13, 0x95f1f4 },
            { 0x63f984, 0xff421f },
            };

    public RecyclerViewAdapter(int expandedHeight, int collapsedHeight, View.OnTouchListener onTouchListener) {
        this.expandedHeight = expandedHeight;
        this.collapsedHeight = collapsedHeight;
        this.onTouchListener = onTouchListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.card_layout, parent, false);
        view.setOnTouchListener(onTouchListener);
        // that is how you can change heights programmatically:
        view.getLayoutParams().height = collapsedHeight;
        view.findViewById(R.id.content).getLayoutParams().height = expandedHeight;
        return new CardHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(position < 0 || position >= getItemCount()) {
            throw (new IllegalArgumentException(String.format("tiles doesn't have position %s", position)));
        }

        holder.itemView.findViewById(R.id.colour).setBackgroundColor(cardColoursArray[position][0]);
        CardHolder.createGradient((ImageView) holder.itemView.findViewById(R.id.gradient), cardColoursArray[position][1]);
    }

    // called somewhere between view came out of sight and not yet reused (called before onBindViewHolder)
    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        Log.d("RecyclerViewAdapter", String.format("onViewRecycled: %s", holder));
    }

    @Override
    public int getItemCount() {
        return cardColoursArray.length;
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        Log.d("PhoneCardAdapter", String.format("onFailedToRecycleView: %s", holder));
        return true; // here we can come when view's animation is in transient state
        // just assure RecyclerView that this view can still be used again. Returning true doesn't mean,
        // that onViewRecycled wount be called.
    }
}

