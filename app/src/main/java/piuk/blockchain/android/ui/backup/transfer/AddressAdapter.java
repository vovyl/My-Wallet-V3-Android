package piuk.blockchain.android.ui.backup.transfer;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import piuk.blockchain.android.R;
import piuk.blockchain.android.data.currency.CurrencyFormatManager;
import piuk.blockchain.android.databinding.ItemAddressBinding;
import piuk.blockchain.android.databinding.SpinnerItemBinding;
import piuk.blockchain.android.ui.account.ItemAccount;

public class AddressAdapter extends ArrayAdapter<ItemAccount> {

    private boolean showText;

    public AddressAdapter(Context context,
                          int textViewResourceId,
                          List<ItemAccount> accountList,
                          boolean showText) {
        super(context, textViewResourceId, accountList);
        this.showText = showText;
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
            binding.tvBalance.setText(item.getDisplayBalance());

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
