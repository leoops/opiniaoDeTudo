package com.example.opiniaodetudo.View

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.view.*
import android.widget.*
import com.example.opiniaodetudo.R
import com.example.opiniaodetudo.model.Review
import com.example.opiniaodetudo.model.ReviewRepository
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream

class FormFragment :Fragment() {

    private lateinit var mainView: View
    private var file: File? = null
    private var thumbnailBytes: ByteArray? = null


    companion object {
        val TAKE_PICTURE_RESULT = 101
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        mainView = inflater.inflate(R.layout.new_review_form_layout, null)

        val buttonSave = mainView.findViewById<Button>(R.id.bt_record)
        val textViewName = mainView.findViewById<EditText>(R.id.et_nameInfor)
        val textViewReview = mainView.findViewById<EditText>(R.id.et_op)
        val imageViewThumbnails = mainView.findViewById<ImageView>(R.id.iv_thumbnail)

        val reviewToEdit = (activity!!.intent?.getSerializableExtra("item") as Review?)?.also { review ->
            textViewName.setText(review.name)
            textViewReview.setText(review.review)
        }

        buttonSave.setOnClickListener {
            val name = textViewName.text
            val review = textViewReview.text
            object : AsyncTask<Void, Void, Unit>() {
                override fun doInBackground(vararg params: Void?) {
                    val repository = ReviewRepository(activity!!.applicationContext)
                    if (reviewToEdit == null) {
                        repository.save(name.toString(), review.toString(), file?.toRelativeString(activity!!.filesDir), thumbnailBytes)
                    }
                }

                override fun onPostExecute(result: Unit?) {
                    if (requireActivity() is MainActivity) {
                        (activity as MainActivity).navigateTo(MainActivity.LIST_FRAGMENT)
                    }
                }
            }.execute()
            true
        }

        configurePhotoClick()
        return mainView

    }

    private fun generateThumbanilBytes(thumbnail: Bitmap, targetSize: Int) {
      val thumbnailOutputStream = ByteArrayOutputStream()
        thumbnail.compress(Bitmap.CompressFormat.PNG, targetSize, thumbnailOutputStream)
        thumbnailBytes = thumbnailOutputStream.toByteArray()
    }

    private fun configurePhotoClick() {
        mainView.findViewById<ImageView>(R.id.iv_thumbnail).setOnClickListener {
            val fileName = "${System.nanoTime()}.jpg"
            file = File(activity!!.filesDir, fileName)
            val uri = FileProvider.getUriForFile(activity!!,
                "com.androiddesenv.opiniaodetudo.fileprovider", file!!)
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            startActivityForResult(intent, TAKE_PICTURE_RESULT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if( resultCode == Activity.RESULT_OK) {
            if(requestCode == TAKE_PICTURE_RESULT) {
                val photoView = mainView.findViewById<ImageView>(R.id.iv_thumbnail)

                val bitmap = BitmapFactory.decodeStream(FileInputStream(file))
                val targetSize = 100
                val thumbnail = ThumbnailUtils.extractThumbnail(
                    bitmap,
                    targetSize,
                    targetSize
                )
                photoView.setImageBitmap(thumbnail)
                generateThumbanilBytes(thumbnail, targetSize)
            }
        } else {
          Toast.makeText(activity, "Erro ao tirar a foto", Toast.LENGTH_SHORT)
        }
    }
}