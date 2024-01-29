package com.github.kr328.clash.design.preference

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.github.kr328.clash.design.R
import com.github.kr328.clash.design.dialog.requestModelTextInput
import com.github.kr328.clash.design.util.Validator
import com.github.kr328.clash.design.util.ValidatorAcceptAll
import com.github.kr328.clash.design.util.ValidatorRangePort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.reflect.KMutableProperty0

interface EditableTextPreference : ClickablePreference {
    var placeholder: CharSequence?
    var empty: CharSequence?
    var text: String?
}

fun <T> PreferenceScreen.editableText(
    value: KMutableProperty0<T>,
    adapter: NullableTextAdapter<T>,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    @StringRes placeholder: Int? = null,
    @StringRes empty: Int? = null,
    configure: EditableTextPreference.() -> Unit = {},
): EditableTextPreference {
    val impl = object : EditableTextPreference, ClickablePreference by clickable(title, icon) {
        override var placeholder: CharSequence? = null
        override var empty: CharSequence? = null
        override var text: String? = null
            set(value) {
                field = value

                when {
                    value == null -> {
                        this.summary = this.placeholder
                    }
                    value.isEmpty() -> {
                        this.summary = this.empty
                    }
                    else -> {
                        this.summary = value
                    }
                }
            }
    }

    if (placeholder != null) {
        impl.placeholder = context.getText(placeholder)
    }

    if (empty != null) {
        impl.empty = context.getText(empty)
    }

    impl.configure()

    launch(Dispatchers.Main) {
        impl.text = withContext(Dispatchers.IO) {
            adapter.from(value.get())
        }

        impl.clicked {
            this@editableText.launch(Dispatchers.Main) {
                val text = context.requestModelTextInput(
                    initial = impl.text,
                    title = impl.title,
                    reset = context.getText(R.string.reset),
                    hint = impl.title,
                )

                val newValue = withContext(Dispatchers.IO) {
                    adapter.to(text).apply(value::set)
                }

                impl.text = adapter.from(newValue)
            }
        }
    }

    return impl
}

fun <T>PreferenceScreen.editableInit(
    value: KMutableProperty0<T>,
    adapter: NotNullTextAdapter<T>,
    default: T,
    error: String = "have error",
    validator: Validator = ValidatorAcceptAll,
    @StringRes title: Int,
    @DrawableRes icon: Int? = null,
    @StringRes placeholder: Int? = null,
    @StringRes empty: Int? = null,
    configure: EditableTextPreference.() -> Unit = {},
) : EditableTextPreference {
    val impl = object : EditableTextPreference, ClickablePreference by clickable(title, icon) {
        override var placeholder: CharSequence? = null
        override var empty: CharSequence? = null
        override var text: String? = null
            set(value) {
                field = value
                this.summary = value
//                when {
//                    value == null -> {
//                        this.summary = this.placeholder
//                    }
//                    value.isEmpty() -> {
//                        this.summary = this.empty
//                    }
//                    else -> {
//                        this.summary = value
//                    }
//                }
            }
    }

    impl.configure()

    launch(Dispatchers.Main) {
        impl.text = withContext(Dispatchers.IO) {
            adapter.from(value.get(), default)
        }
        impl.clicked {
            this@editableInit.launch(Dispatchers.Main) {
                val text = context.requestModelTextInput(
                    initial = impl.text ?: "",
                    title = impl.title,
                    error = error,
                    validator = validator)
                val newValue = withContext(Dispatchers.IO) {
                    adapter.to(text, default).apply(value::set)
                }
                impl.text = adapter.from(newValue, default)
            }
        }
    }

    return impl

}