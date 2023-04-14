package com.wheic.arapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;


import java.lang.reflect.Field;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private ArFragment arFragment;
    private int clickCount = 0;
    private AnchorNode anchorNode;
    private TransformableNode model;

    int[] resourceIds = new int[]{
            R.raw.model,
            R.raw.guirtarglb
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkSystemSupport(this)) {
            arFragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.ar_camera_area);
            if (arFragment != null) {
                arFragment.setOnTapArPlaneListener(this::onPlaneTap);
            }
        }
    }

    private void onPlaneTap(HitResult hitResult, Plane plane, MotionEvent motionEvent) {
        clickCount++;

        ModelRenderable.builder()
                .setSource(this, resourceIds[clickCount % resourceIds.length])
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(modelRenderable -> addModelToScene(hitResult.createAnchor(), modelRenderable))
                .exceptionally(throwable -> {
                    new AlertDialog.Builder(this).setMessage("Something is not right" + throwable.getMessage()).show();
                    return null;
                });
    }

    private void addModelToScene(Anchor anchor, ModelRenderable modelRenderable) {
        if (anchorNode != null && model != null) {
            // Remove the existing model from the scene
            model.setParent(null);
            anchorNode.setParent(null);
        }

        // Create a new AnchorNode and attach it to the scene
        anchorNode = new AnchorNode(anchor);
        anchorNode.setParent(arFragment.getArSceneView().getScene());

        // Create a new TransformableNode and attach it to the anchorNode
        model = new TransformableNode(arFragment.getTransformationSystem());
        model.setParent(anchorNode);
        model.setRenderable(modelRenderable);
        model.select();
    }

    public static boolean checkSystemSupport(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            String openGlVersion = ((ActivityManager) Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE))).
                    getDeviceConfigurationInfo().getGlEsVersion();
            if (Double.parseDouble(openGlVersion) >= 3.0) {
                return true;
            } else {
                showToastAndFinish(activity, "App needs OpenGl Version 3.0 or later");
                return false;
            }
        } else {
            showToastAndFinish(activity, "App does not support required Build Version");
            return false;
        }
    }

    private static void showToastAndFinish(Activity activity, String message) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        activity.finish();
    }
}
