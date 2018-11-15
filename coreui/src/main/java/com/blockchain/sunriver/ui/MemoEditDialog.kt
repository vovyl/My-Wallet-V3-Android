package com.blockchain.sunriver.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import com.blockchain.transactions.Memo
import info.blockchain.wallet.util.HexUtils
import io.reactivex.disposables.CompositeDisposable
import piuk.blockchain.androidcoreui.R
import piuk.blockchain.androidcoreui.utils.extensions.goneIf
import java.lang.Exception

class MemoEditDialog : DialogFragment() {

    private val compositeDisposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.dialog_edit_memo,
        container,
        false
    ).apply {
        isFocusableInTouchMode = true
        requestFocus()
        dialog.window.setWindowAnimations(R.style.DialogNoAnimations)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<View>(R.id.button_ok).setOnClickListener {
            setResultAndDismiss()
        }

        ensureTextIsALong(view)

        view.findViewById<Spinner>(R.id.memo_type_spinner)
            .also { spinner ->
                spinner.setupOptions(view.context)
                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                    override fun onItemSelected(parent: AdapterView<*>, spinner: View, pos: Int, id: Long) {
                        fieldsAndTypes.forEachIndexed { index, (itemId) ->
                            view.findViewById<View>(itemId).update(pos, itemPosition = index)
                        }
                        validate(view)
                    }

                    private fun View.update(selectedPosition: Int, itemPosition: Int) {
                        goneIf(selectedPosition != itemPosition)
                        if (selectedPosition == itemPosition) post { requestFocus() }
                        setOnKeyListener { _, keyCode, _ ->
                            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                setResultAndDismiss()
                                return@setOnKeyListener true
                            }
                            return@setOnKeyListener false
                        }
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                    }
                }

                populateFromArguments(spinner)
            }

        showKeyboard(view.context)
    }

    private fun ensureTextIsALong(view: View) {
        val validator = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validate(view)
            }
        }
        fieldsAndTypes.forEach { (id) ->
            view.findViewById<EditText>(id).addTextChangedListener(validator)
        }
    }

    private fun validate(view: View) {
        val value = enteredValue()
        val valid = when (selectedIndex()) {
            0 -> value.length <= 28
            1 -> isValidId(value)
            2 -> isValidHash(value)
            else -> true
        }
        view.findViewById<View>(R.id.button_ok).isEnabled = valid
    }

    private fun isValidId(s: String): Boolean {
        return try {
            s.toLong()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun isValidHash(s: String): Boolean {
        return try {
            HexUtils.decodeHex(s.toCharArray()).size == 32
        } catch (e: Exception) {
            false
        }
    }

    private fun populateFromArguments(spinner: Spinner) {
        arguments?.let {
            val argType = it.getString("TYPE")
            val index = fieldsAndTypes.indexOfFirst { (_, type) -> type == argType }
            spinner.setSelection(index)
            textView(index).text = it.getString("VALUE")
        }
    }

    private fun showKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun setResultAndDismiss() {
        targetFragment?.onActivityResult(targetRequestCode,
            Activity.RESULT_OK,
            Intent().apply {
                putExtra("TYPE", selectedType())
                putExtra("VALUE", enteredValue())
            }
        )
        dismiss()
    }

    private fun enteredValue(): String =
        textView(selectedIndex()).text.toString()

    private fun textView(selectedIndex: Int): TextView =
        view!!.findViewById(findFieldAndType(selectedIndex).first)

    private fun selectedType() =
        findFieldAndType(selectedIndex()).second

    private val fieldsAndTypes = listOf(
        Pair(R.id.memo_text, "text"),
        Pair(R.id.memo_id, "id"),
        Pair(R.id.memo_hash, "hash")
    )

    private fun findFieldAndType(selectedIndex: Int) = fieldsAndTypes[selectedIndex]

    private fun selectedIndex() = view!!.findViewById<Spinner>(R.id.memo_type_spinner).selectedItemPosition

    private fun Spinner.setupOptions(context: Context) {
        ArrayAdapter.createFromResource(
            context,
            R.array.xlm_memo_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            this.adapter = adapter
        }
    }

    override fun onPause() {
        compositeDisposable.clear()
        super.onPause()
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    companion object {

        fun toMemo(intent: Intent?): Memo {
            if (intent == null) return Memo.None
            return Memo(value = intent.extras.getString("VALUE"), type = intent.extras.getString("TYPE"))
        }

        fun create(memo: Memo): DialogFragment =
            MemoEditDialog().apply {
                arguments = Bundle().apply {
                    putString("VALUE", memo.value)
                    putString("TYPE", memo.type ?: "text")
                }
            }
    }
}
