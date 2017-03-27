package com.hanoch.greatrecipes.control;


import android.os.Bundle;
import android.view.MenuItem;

import com.hanoch.greatrecipes.model.MyFragment;

import java.util.ArrayList;


public interface ToolbarMenuSetting {

    void setToolbarAttr(ArrayList<Integer> toolbarButtonsList, int color, String title);

}
