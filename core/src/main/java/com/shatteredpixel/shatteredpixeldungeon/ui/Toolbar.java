/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2019 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.ui.Tool.QuickslotTool;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSprite;
import com.shatteredpixel.shatteredpixeldungeon.tiles.DungeonTerrainTilemap;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.watabou.input.GameAction;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.noosa.Gizmo;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Button;
import com.watabou.noosa.ui.Component;
import com.watabou.utils.Point;
import com.watabou.utils.PointF;

public class Toolbar extends Component {

	private Tool btnWait;
	private Tool btnSearch;
	private Tool btnInventory;
	private Tool.QuickslotTool[] btnQuick;
	
	private PickedUpItem pickedUp;
	
	private boolean lastEnabled = true;
	public boolean examining = false;

	private static Toolbar instance;

	public enum Mode {
		SPLIT,
		GROUP,
		CENTER
	}
	
	public Toolbar() {
		super();

		instance = this;

		height = btnInventory.height();
	}



	@Override
	protected void createChildren() {

		btnQuick = new QuickslotTool[SPDAction.QUICKSLOT_COUNT];

		// TODO: comment why these are constructed in reverse-order
		for (int slotNum = SPDAction.QUICKSLOT_COUNT-1; slotNum >=0; slotNum--) {
			btnQuick[slotNum] = new Tool.QuickslotTool(this,
					new Visual.Footprint(24, 0, 20, 26),
					slotNum);
			add(btnQuick[slotNum]);
		}
		btnWait = new Tool.WaitTool(this, new Visual.Footprint(24, 0, 20, 26));
		add(btnWait);
		RestButton btnRest = new RestButton(this); //No positional coordinates!
		add(btnRest);
		btnSearch = new Tool.SearchTool(this,new Visual.Footprint(44, 0, 20, 26));
	    add(btnSearch);


		btnInventory = new Tool.InventoryTool
				 (this,new Visual.Footprint(0, 0, 24, 26) );
		add(btnInventory);
		add(pickedUp = new PickedUpItem());
	}
	
	@Override
	protected void layout() {
		// Build border/frame for quicklots
		for(int i = 0; i < SPDAction.QUICKSLOT_COUNT; i++) {
			QuickslotTool button = btnQuick[i];
			int height = 24;
			// Why are the x positions different? What are they measuring?
			if (i == 0 && !SPDSettings.flipToolbar() ||
				i == (SPDAction.QUICKSLOT_COUNT-1) && SPDSettings.flipToolbar()){
				//  Border/frame Left button
				button.border(0, 2);
				button.frame(new Visual.Footprint(106, 0, 19, height));
			} else if (i == 0 && SPDSettings.flipToolbar() ||
					i == (SPDAction.QUICKSLOT_COUNT-1) && !SPDSettings.flipToolbar()){
				//  Border/frame Right button
				button.border(2, 1);
				button.frame(new Visual.Footprint(86, 0, 20, height));
			} else {
				// Border/frame middle buttons
				button.border(0, 1);
				button.frame(new Visual.Footprint(88, 0, 18, height));
			}
		}

		// Cursor for right-side of next button to draw.
		float rightCursor = width;
		switch(Mode.valueOf(SPDSettings.toolbarMode())){
			case SPLIT: {
				btnWait.setPos(x, y);
				btnSearch.setPos(btnWait.right(), y);

				btnInventory.setPos(rightCursor - btnInventory.width(), y);
				Tool previousButton = btnInventory;
				for (int i = 0; i < SPDAction.QUICKSLOT_COUNT; i++) {
					QuickslotTool button = btnQuick[i];
					button.setPos(previousButton.left() - button.width(), y + button.borderTop);
					previousButton = button;
				}


				//center the quickslots if they
				if (btnQuick[SPDAction.QUICKSLOT_COUNT-1].left() < btnSearch.right()) {
					float diff = Math.round(btnSearch.right() - btnQuick[SPDAction.QUICKSLOT_COUNT-1].left()) / 2;
					for (int i = 0; i < SPDAction.QUICKSLOT_COUNT; i++) {
						QuickslotTool button = btnQuick[i];
						button.setPos(button.left() + diff, button.top());
					}
				}

				break;
			}
			//center = group but.. well.. centered, so all we need to do is pre-emptively set the right side further in.
			case CENTER: {
				float toolbarWidth = btnWait.width() + btnSearch.width() + btnInventory.width();
				for (Button slot : btnQuick) {
					if (slot.visible) toolbarWidth += slot.width();
				}
				rightCursor = (width + toolbarWidth) / 2;
			}
			// fallthrough
			case GROUP: {
				btnWait.setPos(rightCursor - btnWait.width(), y);
				btnSearch.setPos(btnWait.left() - btnSearch.width(), y);
				btnInventory.setPos(btnSearch.left() - btnInventory.width(), y);

				Tool previousButton = btnInventory;
				for (int i = 0; i < SPDAction.QUICKSLOT_COUNT; i++) {
					QuickslotTool button = btnQuick[i];
					button.setPos(previousButton.left() - button.width(), y + button.borderTop);
					previousButton = button;
				}

				if (btnQuick[SPDAction.QUICKSLOT_COUNT - 1].left() < 0) {
					float diff = -Math.round(btnQuick[SPDAction.QUICKSLOT_COUNT - 1].left()) / 2;
					for (int i = 0; i < SPDAction.QUICKSLOT_COUNT; i++) {
						QuickslotTool button = btnQuick[i];
						button.setPos(button.left() + diff, button.top());
					}
				}
				break;
			}
		}
		rightCursor = width;

		if (SPDSettings.flipToolbar()) {

			btnWait.setPos( (rightCursor - btnWait.right()), y);
			btnSearch.setPos( (rightCursor - btnSearch.right()), y);
			btnInventory.setPos( (rightCursor - btnInventory.right()), y);

			for(int i = 0; i <= 3; i++) {
				btnQuick[i].setPos( rightCursor - btnQuick[i].right(), y+2);
			}

		}

	}

	public static void updateLayout(){
		if (instance != null) instance.layout();
	}
	
	@Override
	public void update() {
		super.update();
		
		if (lastEnabled != (Dungeon.hero.ready && Dungeon.hero.isAlive())) {
			lastEnabled = (Dungeon.hero.ready && Dungeon.hero.isAlive());
			
			for (Gizmo tool : members) {
				if (tool instanceof Tool) {
					((Tool)tool).enable( lastEnabled );
				}
			}
		}
		
		if (!Dungeon.hero.isAlive()) {
			btnInventory.enable(true);
		}
	}
	
	public void pickup( Item item, int cell ) {
		pickedUp.reset( item,
			cell,
			btnInventory.centerX(),
			btnInventory.centerY());
	}
	
	static CellSelector.Listener informer = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer cell ) {
			instance.examining = false;
			GameScene.examineCell( cell );
		}
		@Override
		public String prompt() {
			return Messages.get(Toolbar.class, "examine_prompt");
		}
	};


	public static class PickedUpItem extends ItemSprite {
		
		private static final float DURATION = 0.5f;
		
		private float startScale;
		private float startX, startY;
		private float endX, endY;
		private float left;
		
		public PickedUpItem() {
			super();
			
			originToCenter();
			
			active =
			visible =
				false;
		}
		
		public void reset( Item item, int cell, float endX, float endY ) {
			view( item );
			
			active =
			visible =
				true;
			
			PointF tile = DungeonTerrainTilemap.raisedTileCenterToWorld(cell);
			Point screen = Camera.main.cameraToScreen(tile.x, tile.y);
			PointF start = camera().screenToCamera(screen.x, screen.y);
			
			x = this.startX = start.x - ItemSprite.SIZE / 2;
			y = this.startY = start.y - ItemSprite.SIZE / 2;
			
			this.endX = endX - ItemSprite.SIZE / 2;
			this.endY = endY - ItemSprite.SIZE / 2;
			left = DURATION;
			
			scale.set( startScale = Camera.main.zoom / camera().zoom );
			
		}
		
		@Override
		public void update() {
			super.update();
			
			if ((left -= Game.elapsed) <= 0) {
				
				visible =
				active =
					false;
				if (emitter != null) emitter.on = false;
				
			} else {
				float p = left / DURATION;
				scale.set( startScale * (float)Math.sqrt( p ) );
				
				x = startX*p + endX*(1-p);
				y = startY*p + endY*(1-p);
			}
		}
	}


	static class ToolbarButton extends Button {
		protected Toolbar toolbar;
		public ToolbarButton(Toolbar t) {
			this.toolbar = t;
		}
	}

	static class RestButton extends ToolbarButton {
		public RestButton(Toolbar t) {
			super(t);
		}
		@Override
		protected void onClick() {
			toolbar.examining = false;
			Dungeon.hero.rest(true);
		}

		@Override
		public GameAction keyAction() {
			return SPDAction.REST;
		}
	}
}
