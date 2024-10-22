package student.newinti.spendee;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Collections;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.stream.Collectors;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;
    // retrieve and store the transaction record in transaction list
    public TransactionAdapter(List<Transaction> transactionList) {

        Collections.sort(transactionList, new Comparator<Transaction>() {
            @Override
            public int compare(Transaction t1, Transaction t2) {
                return t1.getDate().compareTo(t2.getDate()); // sort records in descending
            }
        });
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    // create and inflate the view for each item (transaction) in the RecyclerView
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override //bind the transaction data records to the TextViews for the current position
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactionList.get(position);

        holder.amountTextView.setText(String.format(Locale.getDefault(), "RM%.2f", transaction.getAmount()));
        holder.typeTextView.setText(transaction.getType());
        holder.categoryTextView.setText(transaction.getCategory());
        holder.descriptionTextView.setText(transaction.getDescription());
        holder.dateTextView.setText(transaction.getDate());
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }
//inner class that represents the ViewHolder for a transaction item
    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView amountTextView, typeTextView, categoryTextView, descriptionTextView, dateTextView;
        // constructor that binds the TextViews with their corresponding Ids from the layout
        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            amountTextView = itemView.findViewById(R.id.amount_text_view);
            typeTextView = itemView.findViewById(R.id.type_text_view);
            categoryTextView = itemView.findViewById(R.id.category_text_view);
            descriptionTextView = itemView.findViewById(R.id.description_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
        }
    }
}
