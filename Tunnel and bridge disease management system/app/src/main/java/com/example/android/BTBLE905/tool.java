package com.example.android.BTBLE905;

import android.animation.ObjectAnimator;
import android.view.View;

/**
 * @author Admin
 * @version $Rev$
 * @des ${TODO}
 * @updateAuthor $Author$
 * @updateDes ${TODO}
 */
public  class tool {
    public void  showview(View view){
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0, 1);
        alpha.setDuration(1000);
        scaleY.setDuration(500);
        view.setPivotX(view.getWidth()/2);
        view.setPivotY(0);
        scaleY.start();
        alpha.start();
    }
    public void hideview(View view){
       ObjectAnimator alpha= ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1, 1/2);
        alpha.setDuration(500);
        scaleY.setDuration(1000);
        view.setPivotX(view.getWidth()/2);
        view.setPivotY(0);
        scaleY.start();
        alpha.start();
    }

}
