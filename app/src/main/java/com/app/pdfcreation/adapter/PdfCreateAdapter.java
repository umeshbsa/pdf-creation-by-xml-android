package com.app.pdfcreation.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.pdfcreation.R;
import com.app.pdfcreation.model.PDFModel;

import java.util.List;

public class PdfCreateAdapter extends RecyclerView.Adapter<PdfCreateAdapter.MyViewHolder> {

    private List<PDFModel> pdfModels;

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pdf_creation, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        PDFModel model = pdfModels.get(position);
        if (model != null) {
            if (model.isReceived()) {
                holder.mReceivedTV.setVisibility(View.VISIBLE);
            } else {
                holder.mReceivedTV.setVisibility(View.GONE);
            }

            holder.mPriceTV.setText(model.getPrice());
            holder.mNameTV.setText(model.getName());
            int ratingDrawable = getRatingImage(model.getRating());
            holder.mRateIM.setImageResource(ratingDrawable);
        }
    }

    @Override
    public int getItemCount() {
        return pdfModels.size();
    }

    /**
     * This is set from PDFCreateByXML class
     * This is my own model. This model have to set data from api
     *
     * @param pdfModels
     */
    public void setListData(List<PDFModel> pdfModels) {
        this.pdfModels = pdfModels;
        notifyDataSetChanged();
    }

    /**
     * Set rating image
     *
     * @param rating this is getting from api and set to image by rate point
     * @return
     */
    private int getRatingImage(float rating) {
        int image = 0;
        if (rating == 0f) {
            image = R.drawable.pdf_star_1;
        } else if (rating == 0.5f) {
            image = R.drawable.pdf_half_star_2;
        } else if (rating == 1f) {
            image = R.drawable.pdf_star_2;
        } else if (rating == 1.5f) {
            image = R.drawable.pdf_half_star_3;
        } else if (rating == 2f) {
            image = R.drawable.pdf_star_3;
        } else if (rating == 2.5f) {
            image = R.drawable.pdf_half_star_4;
        } else if (rating == 3f) {
            image = R.drawable.pdf_star_4;
        } else if (rating == 3.5f) {
            image = R.drawable.pdf_half_star_5;
        } else if (rating == 4f) {
            image = R.drawable.pdf_star_5;
        } else if (rating == 4.5f) {
            image = R.drawable.pdf_half_star_6;
        } else if (rating == 5f) {
            image = R.drawable.pdf_star_6;
        }
        return image;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView mReceivedTV;
        private final TextView mNameTV;
        private final ImageView mRateIM;
        private final TextView mPriceTV;

        public MyViewHolder(View view) {
            super(view);
            mPriceTV = (TextView) view.findViewById(R.id.tv_price);
            mReceivedTV = (TextView) view.findViewById(R.id.tv_received);
            mNameTV = (TextView) view.findViewById(R.id.tv_name);
            mRateIM = (ImageView) view.findViewById(R.id.iv_rate);
        }
    }

}