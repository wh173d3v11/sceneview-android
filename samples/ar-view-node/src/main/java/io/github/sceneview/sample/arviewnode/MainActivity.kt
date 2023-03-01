package io.github.sceneview.sample.arviewnode

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.sceneform.rendering.ViewRenderable
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.model.await
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.ViewNode
import io.github.sceneview.utils.doOnApplyWindowInsets
import io.github.sceneview.utils.setFullScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private lateinit var sceneView: ArSceneView
    private lateinit var loadingView: View
    private lateinit var placeModelButton: ExtendedFloatingActionButton

    private lateinit var arNode: ArModelNode
    private lateinit var viewNode: ViewNode
    private lateinit var modelNode: ModelNode

    private var isLoading = false
        set(value) {
            field = value
            loadingView.isGone = !value
        }
    lateinit var textView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        textView = TextView(this)
        setFullScreen(
            findViewById(R.id.rootView),
            fullScreen = true,
            hideSystemBars = false,
            fitsSystemWindows = false
        )

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar)?.apply {
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).topMargin = systemBarsInsets.top
            }
            title = ""
        })

        sceneView = findViewById(R.id.sceneView)
        loadingView = findViewById(R.id.loadingView)

        placeModelButton = findViewById<ExtendedFloatingActionButton>(R.id.placeModelButton).apply {
            // Add system bar margins
            val bottomMargin = (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin
            doOnApplyWindowInsets { systemBarsInsets ->
                (layoutParams as ViewGroup.MarginLayoutParams).bottomMargin =
                    systemBarsInsets.bottom + bottomMargin
            }
            setOnClickListener { placeArNode() }
        }

        arNode = ArModelNode(placementMode = PlacementMode.PLANE_HORIZONTAL).apply {
            parent = sceneView
            applyPoseRotation = true
        }

        //working
//        ViewRenderable.builder()
//            .setView(this, textView)
//            .build(lifecycle)
//            .thenAccept { renderable: ViewRenderable ->
//                viewNode = ViewNode()
//                viewNode.parent = modelNode
//                viewNode.setRenderable(renderable)
//                viewNode.position = Position(x = 0.0f, y = 0.0f, z = 0.0f)
//                startCounting()
//            }

        // Create an instance of the layout inflater
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        // Inflate the layout you want to add
        val newView: View = inflater.inflate(R.layout.view_node, null)
        viewNode = ViewNode().apply { //working
            parent = arNode
            lifecycle.coroutineScope.launchWhenCreated {
                val renderable = ViewRenderable.builder()
                    .setView(this@MainActivity, newView)
                    .await(lifecycle)
                setRenderable(renderable)
                val rotation = newView.findViewById<TextView>(R.id.rotation)
                startCounting(rotation)
            }
            isEditable = false
        }
//        viewNode = ViewNode().apply { //working
//            parent = arNode
//            lifecycle.coroutineScope.launchWhenCreated {
//                val renderable = ViewRenderable.builder()
//                    .setView(this@MainActivity, textView)
//                    .await(lifecycle)
//                setRenderable(renderable)
//                startCounting()
//            }
//            isEditable = false
//        }

        isLoading = true

        // "Planet" (https://skfb.ly/o9w6U) by userwaniroll is licensed under Creative Commons Attribution (http://creativecommons.org/licenses/by/4.0/)
        modelNode = ModelNode(this, "models/planet.glb", scaleUnits = 1f).apply {
            parent = arNode
            position.y = 0.8f
            onModelLoaded += {
                isLoading = false
            }
        }
    }

    private fun startCounting(rotation: TextView = textView) {
        lifecycleScope.launch {
            for (i in 0..1000) {
                delay(1000)
                rotation.text = "COUNT DOWN --> $i"
            }
        }
    }

    private fun placeArNode() {
        arNode.anchor()
        placeModelButton.isVisible = false
        sceneView.planeRenderer.isVisible = false
    }
}