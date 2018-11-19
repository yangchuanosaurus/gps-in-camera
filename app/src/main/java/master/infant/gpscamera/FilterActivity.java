package master.infant.gpscamera;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class FilterActivity extends AppCompatActivity {

    private static final String TAG = FilterActivity.class.getSimpleName();

    public static void start(Context context) {
        Intent intent = new Intent(context, FilterActivity.class);
        context.startActivity(intent);
    }

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_filter);

        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerAdapter = new RecyclerViewAdapter(new OnItemClickListener() {
            @Override
            public void onItemClick(int pos) {
                Log.d(TAG, "onItemClick position = " + pos);
                //mRecyclerAdapter.notifyItemChanged(pos);
            }
        });

        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    interface OnItemClickListener {
        void onItemClick(int pos);
    }

    private static class RecyclerViewAdapter extends RecyclerView.Adapter<Vh> {

        private OnItemClickListener mItemListener;

        public RecyclerViewAdapter(OnItemClickListener itemListener) {
            mItemListener = itemListener;
        }

        @NonNull
        @Override
        public Vh onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View rowView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.row_filter, viewGroup, false);
            return new Vh(rowView, mItemListener);
        }

        @Override
        public void onBindViewHolder(@NonNull Vh vh, int i) {

        }

        @Override
        public int getItemCount() {
            return 10;
        }
    }

    private static class Vh extends RecyclerView.ViewHolder {

        CheckBox mCheckBox;

        public Vh(@NonNull View itemView, final OnItemClickListener itemListener) {
            super(itemView);

            mCheckBox = itemView.findViewById(R.id.checkbox);
            mCheckBox.setClickable(false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleCheckBox();
                    itemListener.onItemClick(getAdapterPosition());
                }
            });
        }

        private void toggleCheckBox() {
            boolean checked = mCheckBox.isChecked();
            Log.d(TAG, "-------------" + checked);
            mCheckBox.setChecked(!checked);
        }
    }

}
