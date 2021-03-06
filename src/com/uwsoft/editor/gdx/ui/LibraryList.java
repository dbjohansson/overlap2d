/*
 * ******************************************************************************
 *  * Copyright 2015 See AUTHORS file.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *  *****************************************************************************
 */

package com.uwsoft.editor.gdx.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.uwsoft.editor.gdx.stage.UIStage;
import com.uwsoft.editor.gdx.ui.dialogs.ConfirmDialog;
import com.uwsoft.editor.gdx.ui.dialogs.InputDialog;
import com.uwsoft.editor.gdx.ui.thumbnailbox.LibraryItemThumbnailBox;
import com.uwsoft.editor.renderer.actor.LabelItem;
import com.uwsoft.editor.renderer.actor.TextBoxItem;
import com.uwsoft.editor.renderer.data.CompositeItemVO;
import com.uwsoft.editor.renderer.data.TextBoxVO;

public class LibraryList extends Group {

    private final HashMap<String, CompositeItemVO> items;
    private final UIStage stage;
    private final Group listContainer;
    private ArrayList<LibraryItemThumbnailBox> libraryItems;
    private LibraryItemThumbnailBox librarySelectedItem;
    private Label searchLbl;
    private TextBoxItem searchText;
    public LibraryList(final UIStage s, float width, float height) {
        stage = s;
        libraryItems = new ArrayList<>();
        this.setWidth(width);
        this.setHeight(height);
        final Table container = new Table();
        Table table = new Table();
        listContainer = new Group();
        container.setX(0);
        container.setY(0);
        container.setWidth(getWidth() - 1);
        container.setHeight(getHeight() - 20);
        listContainer.setWidth(getWidth() - 20);
        listContainer.setHeight(getHeight() - 25);
        final ScrollPane scroll = new ScrollPane(table, s.textureManager.editorSkin);
        container.add(scroll).colspan(4).width(getWidth());
        container.row();
        scroll.addListener(new InputListener() {
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                event.stop();
                return true;
            }
        });
        scroll.setHeight(getHeight() - 20);
        scroll.setY(0);
        scroll.setFlickScroll(false);


        items = s.getSandbox().sceneControl.getCurrentSceneVO().libraryItems;
        //-----------------Search Box
        Group searchGroup   =   new Group();
        container.add(searchGroup);
        searchGroup.setWidth(200);
        searchGroup.setHeight(30);
        searchLbl   =   new Label("Search :", s.textureManager.editorSkin);
        searchGroup.addActor(searchLbl);

        searchText = new TextBoxItem(new TextBoxVO(),s.essentials);
        searchText.setX(searchLbl.getTextBounds().width);
        searchGroup.addActor(searchText);
        searchText.setTextFieldListener(new TextField.TextFieldListener() {
            public void keyTyped (TextField textField, char key) {
                drawItems(searchText.getText());
            }
        });
        Label dummyTst = new Label("dummy", s.textureManager.editorSkin);
        if (items.size() * dummyTst.getHeight() > listContainer.getHeight()) {
            listContainer.setHeight(items.size() * (dummyTst.getHeight() + 2));
        }
        drawItems();
        table.add(listContainer);
        table.row();
        addActor(container);

    }

    private void drawItems() {
        listContainer.clearChildren();
        libraryItems.clear();


        int iter = 1;
        for (final String value : items.keySet()) {
            LibraryItemThumbnailBox thumb = new LibraryItemThumbnailBox(stage, getWidth(), value, items.get(value));
            thumb.setX(0);
            thumb.setY(listContainer.getHeight() - thumb.getHeight() * iter - 2 * iter);
            listContainer.addActor(thumb);
            libraryItems.add(thumb);
            iter++;
        }
        librarySelectedItem = null;
        initListeners();


    }
    private void drawItems(String searchText) {
        listContainer.clearChildren();
        libraryItems.clear();


        int iter = 1;
        for (final String value : items.keySet()) {
            if(!value.contains(searchText))continue;
            LibraryItemThumbnailBox thumb = new LibraryItemThumbnailBox(stage, getWidth(), value, items.get(value));
            thumb.setX(0);
            thumb.setY(listContainer.getHeight() - thumb.getHeight() * iter - 2 * iter);
            listContainer.addActor(thumb);
            libraryItems.add(thumb);
            iter++;
        }
        librarySelectedItem = null;
        initListeners();


    }

    private void initListeners() {
        for (final LibraryItemThumbnailBox libraryItemThumbnailBox : libraryItems) {
            ClickListener listener = new ClickListener() {

                public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                    super.touchUp(event, x, y, pointer, button);
                    if (librarySelectedItem != null) {
                        librarySelectedItem.unselect();
                    }
                    libraryItemThumbnailBox.select();
                    librarySelectedItem = libraryItemThumbnailBox;
                    stage.setKeyboardFocus(libraryItemThumbnailBox);
                    if (getTapCount() == 2) {
                        showRenameDialog();
                    }
                }

                public boolean keyUp(InputEvent event, int keycode) {
                    if (keycode != Input.Keys.DEL) {
                        return false;
                    }
                    showConfirmDialog(librarySelectedItem);

                    return true;
                }
            };
            libraryItemThumbnailBox.addListener(listener);
        }
    }

    private void showConfirmDialog(final LibraryItemThumbnailBox librarySelectedItem) {
        ConfirmDialog confirmDialog = stage.dialogs().showConfirmDialog();
        confirmDialog.setDescription("Are you sure you want to delete library item?");

        confirmDialog.setListener(new ConfirmDialog.ConfirmDialogListener() {
            @Override
            public void onConfirm() {
                items.remove(librarySelectedItem.getKey());
                drawItems();
            }

            @Override
            public void onCancel() {

            }
        });
    }

    private void showRenameDialog() {
        InputDialog dlg = stage.dialogs().showInputDialog();


        dlg.setDescription("New name for your layer");

        dlg.setListener(new InputDialog.InputDialogListener() {

            @Override
            public void onConfirm(String input) {
                items.remove(librarySelectedItem.getKey());
                librarySelectedItem.setKey(input);
                items.put(input, librarySelectedItem.getCompositeItemVO());
            }
        });
    }
}
