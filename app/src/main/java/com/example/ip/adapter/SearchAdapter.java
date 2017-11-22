package com.example.ip.adapter;

import android.content.Context;

import com.example.ip.base.adapter.BaseRecyclerViewHolder;
import com.example.ip.base.adapter.SimpleAdapter;

import java.util.List;

/**
 * Created by zhangyuanyuan on 2017/11/22.
 */

public class SearchAdapter extends SimpleAdapter<String>{

    public SearchAdapter(Context context, List<String>divList) {
        super(context, android.R.layout.simple_list_item_1);
    }

    @Override
    protected void convert(BaseRecyclerViewHolder viewHolder, String item, int pos) {
        viewHolder.getTextView(android.R.id.text1).setText(item);
    }
}
