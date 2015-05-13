package com.iclub.samskrut.omnipresence;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

public class MyRenderer extends RajawaliCardboardRenderer {

    public int projectPos;
    public int pos;

    public MyRenderer(Context context, int pp, int p) {
        super(context);
        projectPos = pp;
        pos = p;
    }

    @Override
    protected void initScene() {

        Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/omnipresence/" + projectPos + "_" + pos + ".jpg");
        Sphere sphere = createPhotoSphereWithTexture(new Texture("photo", bitmap));

        getCurrentScene().addChild(sphere);

        getCurrentCamera().setPosition(Vector3.ZERO);
        getCurrentCamera().setFieldOfView(75);
    }

    private static Sphere createPhotoSphereWithTexture(ATexture texture) {

        Material material = new Material();
        material.setColor(0);

        try {
            material.addTexture(texture);
        } catch (ATexture.TextureException e) {
            throw new RuntimeException(e);
        }

        Sphere sphere = new Sphere(50, 64, 32);
        sphere.setScaleX(-1);
        sphere.setMaterial(material);

        return sphere;
    }
}
