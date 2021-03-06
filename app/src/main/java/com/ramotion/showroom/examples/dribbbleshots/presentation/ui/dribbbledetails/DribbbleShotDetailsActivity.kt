package com.ramotion.showroom.examples.dribbbleshots.presentation.ui.dribbbledetails

import android.graphics.Rect
import android.os.Bundle
import android.text.InputType
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding2.view.RxView
import com.ramotion.showroom.R
import com.ramotion.showroom.databinding.ActivityDribbbleDetailsBinding
import com.ramotion.showroom.examples.dribbbleshots.domain.ImageLoader
import com.ramotion.showroom.examples.dribbbleshots.domain.entity.DribbbleShot
import com.ramotion.showroom.examples.dribbbleshots.presentation.ui.dribbbledetails.DribbbleDetailsIntent.GetDribbbleShot
import com.ramotion.showroom.examples.dribbbleshots.presentation.ui.dribbbledetails.DribbbleDetailsIntent.SaveDribbbleShot
import com.ramotion.showroom.examples.dribbbleshots.utils.BaseView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class DribbbleShotDetailsActivity : AppCompatActivity(), BaseView<DribbbleDetailsState> {

  private var binding: ActivityDribbbleDetailsBinding? = null
  private lateinit var intentsSubscription: Disposable
  private val viewModel: DribbbleDetailsViewModel by viewModel()
  private lateinit var currentState: DribbbleDetailsState
  private val imageLoader: ImageLoader by inject()
  private val shotId: Int by lazy(LazyThreadSafetyMode.NONE) { intent.getIntExtra("shotId", 0) }
  private var navBarHeight: Int = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
    super.onCreate(savedInstanceState)
    handleStates()
    initBinding()
    initEtShotMessage()
    initNavBarHeight()
    initKeyboardWatcher()
    initIntents()
  }

  override fun onDestroy() {
    binding = null
    intentsSubscription.dispose()
    super.onDestroy()
  }

  private fun initBinding() {
    binding = DataBindingUtil.setContentView(this, R.layout.activity_dribbble_details)
  }

  private fun initEtShotMessage() {
    binding!!.etShotMessage.imeOptions = EditorInfo.IME_ACTION_DONE
    binding!!.etShotMessage.setRawInputType(InputType.TYPE_CLASS_TEXT)
  }

  private fun initNavBarHeight() {
    val activityRoot = (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
    activityRoot.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
      override fun onGlobalLayout() {
        activityRoot.viewTreeObserver.removeOnGlobalLayoutListener(this)
        val r = Rect()
        activityRoot.getWindowVisibleDisplayFrame(r)
        val screenHeight = activityRoot.rootView.height
        navBarHeight = screenHeight - r.height()
      }
    })
  }

  private fun initKeyboardWatcher() {
    KeyboardVisibilityEvent.setEventListener(this) { isOpen ->
      binding?.run {
        val r = Rect()
        val activityRoot = (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
        activityRoot.getWindowVisibleDisplayFrame(r)
        val screenHeight = activityRoot.rootView.height
        val heightDiff = screenHeight - (r.height() + navBarHeight)

        imagesGroup.visibility = if (isOpen) GONE else VISIBLE
        bottomTextGuideline.setGuidelineEnd(if (isOpen) heightDiff else 0)
      }
    }
  }

  override fun initIntents() {
    intentsSubscription = Observable.merge(listOf(
        Observable.just(GetDribbbleShot(shotId)),

        RxView.clicks(binding!!.btnSend)
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .map { SaveDribbbleShot(currentState.shot.copy(message = binding!!.etShotMessage.text.toString().trim())) },

        RxView.clicks(binding!!.ivClose)
            .throttleFirst(1000, TimeUnit.MILLISECONDS)
            .doOnNext { onBackPressed() }
    ))
        .subscribe(viewModel.viewIntentsConsumer())
  }

  override fun handleStates() {
    viewModel.stateReceived().observe(this, Observer { state -> render(state) })
  }

  private fun showSuccessDialog() {
    AlertDialog.Builder(this)
        .setCancelable(false)
        .setMessage(R.string.shot_sent_successful_message)
        .setPositiveButton(getString(R.string.ok)) { dialog, which ->
          dialog.dismiss()
          onBackPressed()
        }
        .show()
  }

  override fun render(state: DribbbleDetailsState) {
    if (state.shotSaved) {
      showSuccessDialog()
    }

    if (state.shot != DribbbleShot.EMPTY) {
      imageLoader.loadImage(
          iv = binding!!.ivShotImage,
          url = if (state.shot.imageHi.isNotBlank()) state.shot.imageHi else state.shot.imageNormal,
          centerCrop = true,
          withAnim = true
      )
    }

    binding!!.pbInitLoading.visibility = if (state.loading) VISIBLE else GONE
    binding!!.pbSaveLoading.visibility = if (state.saveLoading) VISIBLE else GONE
    binding!!.btnSend.text = if (state.saveLoading) "" else getString(R.string.send)

    state.error?.run {
      Snackbar.make(binding!!.root, message ?: getString(R.string.error_occurred), Snackbar.LENGTH_LONG).show()
    }

    currentState = state
  }
}
