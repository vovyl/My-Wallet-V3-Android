package piuk.blockchain.android.ui.send;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import piuk.blockchain.android.R;
import piuk.blockchain.android.databinding.ItemAddressBinding;
import piuk.blockchain.android.databinding.SpinnerItemBinding;
import piuk.blockchain.android.ui.account.ItemAccount;

public class AddressAdapter extends ArrayAdapter<ItemAccount> {

    private boolean showText;
    private boolean isBtc;
    private String fiatUnits;
    private double exchangeRate;

    /**
     * Constructor that allows handling both BTC and Fiat
     */
    public AddressAdapter(Context context,
                          int textViewResourceId,
                          List<ItemAccount> accountList,
                          boolean showText,
                          boolean isBtc,
                          String fiatUnits,
                          double exchangeRate) {
        super(context, textViewResourceId, accountList);
        this.showText = showText;
        this.isBtc = isBtc;
        this.fiatUnits = fiatUnits;
        this.exchangeRate = exchangeRate;
    }

    /**
     * BTC only constructor
     */
    public AddressAdapter(Context context,
                          int textViewResourceId,
                          List<ItemAccount> accountList,
                          boolean showText) {
        super(context, textViewResourceId, accountList);
        this.showText = showText;
        isBtc = true;
    }

    public void updateData(List<ItemAccount> accountList) {
        clear();
        addAll(accountList);
        notifyDataSetChanged();
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent, true);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return getCustomView(position, parent, false);
    }

    private View getCustomView(int position, ViewGroup parent, boolean isDropdownView) {

        if (isDropdownView) {
            ItemAddressBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.item_address,
                    parent,
                    false);

            ItemAccount item = getItem(position);

            if (item.getTag() == null || item.getTag().isEmpty()) {
                binding.tvTag.setVisibility(View.GONE);
            } else {
                binding.tvTag.setText(item.getTag());
            }
            binding.tvLabel.setText(item.getLabel());

            if (isBtc) {
                binding.tvBalance.setText(item.getDisplayBalance());
            } else {
                double btcBalance = item.getAbsoluteBalance() != null ? item.getAbsoluteBalance() / 1e8 : 0D;
                double fiatBalance = exchangeRate * btcBalance;

                //todo
//                String balance = monetaryUtil.getFiatFormat(fiatUnits).format(Math.abs(fiatBalance)) + " " + fiatUnits;
                binding.tvBalance.setText("fix me");
            }

            return binding.getRoot();

        } else {
            SpinnerItemBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(getContext()),
                    R.layout.spinner_item,
                    parent,
                    false);

            if (showText) {
                ItemAccount item = getItem(position);
                binding.text.setText(item.getLabel());
            }

            return binding.getRoot();
        }
    }
}
