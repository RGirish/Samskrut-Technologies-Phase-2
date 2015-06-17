package com.iclub.samskrut.omnipresence;

import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.rajawali3d.cardboard.RajawaliCardboardRenderer;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MyRenderer extends RajawaliCardboardRenderer {

    public int projectPos;
    public int pos;
    public ContextWrapper contextWrapper;

    public MyRenderer(ContextWrapper c, int pp, int p) {
        super(c);
        contextWrapper = c;
        projectPos = pp;
        pos = p;
    }

    @Override
    protected void initScene() {

        InputStream is = null;
        try{
            is = contextWrapper.openFileInput(Login.USERNAME+"_"+projectPos + "_" + pos + ".jpg");
        }catch (FileNotFoundException e){
            Log.e("MyRenderer filenotfound",Login.USERNAME+"_"+projectPos + "_" + pos + ".jpg");
        }
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        //Bitmap bitmap = decodeFile(is);
        //Bitmap bitmap = BitmapFactory.decodeStream(is, null, options);
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

    // Decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(InputStream is) {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, o);
        final int REQUIRED_SIZE=1024;
        int scale = 1;
        while(o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE) {
            scale *= 2;
        }
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(is, null, o2);
    }
}