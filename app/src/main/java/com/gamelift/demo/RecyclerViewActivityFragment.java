package com.gamelift.demo;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.gamelift.expandcollapseanimator.ExpandCollapseAnimator;
import com.gamelift.expandcollapseanimator.VerticalClipLayout;

public class RecyclerViewActivityFragment extends Fragment {

    private static final String LOG_TAG = "RecyclerDemo";

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private ExpandCollapseAnimator animator;

    private int lastExpandRequest;
    private static final String LAST_EXPAND_REQUEST = "last_expand_request";
    //scroll is saved (I mean savedInstanceStateBundle) by recyclerView itself

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recycler_view, container, false);

        // check display width to figure out card size keeping aspect
        Point displaySize = new Point();
        ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
        float expandedHeightAspect = 0.7f;
        float collapsedHeightAspect = 0.3f;
        final int expandedHeight = (int)(displaySize.x * expandedHeightAspect);
        final int collapsedHeight = (int)(displaySize.x * collapsedHeightAspect);

        recyclerView = (RecyclerView)view.findViewById(R.id.recycler_view);

        if(savedInstanceState == null) {
            lastExpandRequest = 0;
        } else {
            lastExpandRequest = savedInstanceState.getInt(LAST_EXPAND_REQUEST);
        }

        layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);

        animator = new ExpandCollapseAnimator(0.001f * getResources().getDisplayMetrics().density);

        // recycle view's items appear from the bottom too late when expanded, so we increased recycler view's area to avoid it
        ((ViewGroup.MarginLayoutParams)recyclerView.getLayoutParams()).bottomMargin = (int)(collapsedHeight - expandedHeight + 0.5f);
        recyclerView.setPadding(recyclerView.getPaddingLeft(),
                expandedHeight - collapsedHeight, // first element need space to be expanded somewhere
                recyclerView.getPaddingRight(),
                expandedHeight - collapsedHeight); // all this stuff works together thanks to clipToPadding=false
        recyclerView.setHasFixedSize(true);

        animator.setOnViewExpandCollapseListener(new ExpandCollapseAnimator.OnViewExpandCollapseListener() {

            @Override
            public void onViewStartExpanding(int position, VerticalClipLayout v) {
            }

            @Override
            public void onViewExpanded(int position, VerticalClipLayout v) {
            }

            @Override
            public void onViewStartCollapsing(int position, VerticalClipLayout v) {
            }

            @Override
            public void onViewsChanging() {
            }
        });

        recyclerView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, final View child) {
                child.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        child.getViewTreeObserver().removeOnPreDrawListener(this);

                        int position = recyclerView.getChildAdapterPosition(child);
                        animator.add(position, (VerticalClipLayout) child);
                        if (lastExpandRequest == position) {
                            animator.setExpanding(position); // here we call setExpanding and not just call onStartExpanding() and onExpanded() because animator actually handles animation and decides when to notify listeners
                        }
                        return false;
                    }
                });
            }

            @Override
            public void onChildViewRemoved(View parent, View child) {
                Log.d(LOG_TAG, "Fragment.onChildViewRemoved " + recyclerView.getChildViewHolder(child));
                int position = recyclerView.getChildAdapterPosition(child);
                animator.remove(position);
            }
        });

        adapter = new RecyclerViewAdapter(expandedHeight, collapsedHeight,
                new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {

                if(MotionEvent.ACTION_DOWN == event.getAction()) {
                    return true;
                }
                if (MotionEvent.ACTION_UP != event.getAction()) {
                    return false;
                }

                int clickedPosition = recyclerView.getChildAdapterPosition(view);
                if(clickedPosition != lastExpandRequest) {
                    animator.setExpanding(clickedPosition);
                    lastExpandRequest = clickedPosition;
                }

                return true; //avoid dispatching to view behind
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(LAST_EXPAND_REQUEST, lastExpandRequest);
    }

    @Override
    public void onResume() {
        super.onResume();
        animator.start();
    }

    @Override
    public void onPause() {
        animator.pause();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        recyclerView.setOnHierarchyChangeListener(null);
        recyclerView = null;
        adapter = null;
        animator = null;
        super.onDestroyView();
    }
}
