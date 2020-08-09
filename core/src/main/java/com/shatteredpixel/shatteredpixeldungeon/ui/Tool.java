package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBag;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndJournal;
import com.watabou.input.GameAction;
import com.watabou.noosa.Image;
import com.watabou.noosa.Visual;
import com.watabou.noosa.ui.Button;

public class Tool extends Button {

    private static final int BGCOLOR = 0x7B8073;

    // Parent toolbar this is installed in.
    protected Toolbar toolbar;
    private Image base;

    public Tool(Toolbar toolbar, Visual.Footprint f) {
        super();
        this.toolbar = toolbar;
        hotArea.blockWhenInactive = true;
        frame(f);
    }


    public void frame( Visual.Footprint f) {
        base.frame(f);

        this.width = width;
        this.height = height;
    }

    @Override
    protected void createChildren() {
        super.createChildren();

        base = new Image( Assets.Interfaces.TOOLBAR );
        add( base );
    }

    @Override
    protected void layout() {
        super.layout();

        base.x = x;
        base.y = y;
    }

    @Override
    protected void onPointerDown() {
        base.brightness( 1.4f );
    }

    @Override
    protected void onPointerUp() {
        if (active) {
            base.resetColor();
        } else {
            base.tint( BGCOLOR, 0.7f );
        }
    }

    public void enable( boolean value ) {
        if (value != toolbar.active) {
            if (value) {
                base.resetColor();
            } else {
                base.tint( BGCOLOR, 0.7f );
            }
            active = value;
        }
    }

    static class QuickslotTool extends Tool {

        private QuickSlotButton slot;
        private int borderLeft;
        private int borderRight;
        public int borderTop;
        private int borderBottom;


        public QuickslotTool(Toolbar toolbar, Visual.Footprint f, int slotNum ) {
            super(toolbar,f);
            borderLeft = 2;
            borderRight = 2;
            borderTop = 2;
            borderBottom = 2;
            slot = new QuickSlotButton( slotNum );
            add(slot);
        }

        public void border( int left, int right){
            borderLeft = left;
            borderRight = right;
            layout();
        }

        @Override
        protected void layout() {
            super.layout();
            slot.setRect( x + borderLeft, y + borderTop,
                    width - (borderLeft+borderRight), height - (borderTop+borderBottom));
        }

        @Override
        public void enable( boolean value ) {
            super.enable( value );
            slot.enable( value );
        }
    }

    static class WaitTool extends Tool {
        public WaitTool(Toolbar t, Visual.Footprint f) { super(t, f);
        }

        @Override
        protected void onClick() {
            toolbar.examining = false;
            Dungeon.hero.rest(false);
        }

        @Override
        public GameAction keyAction() {
            return SPDAction.WAIT;
        }

        protected boolean onLongClick() {
            toolbar.examining = false;
            Dungeon.hero.rest(true);
            return true;
        }
    };


    static class ToolbarButton extends Button {
        protected Toolbar toolbar;
      public ToolbarButton(Toolbar toolbar) {
          this.toolbar = toolbar;
        }
    }

    static class RestButton extends ToolbarButton {
        public RestButton(Toolbar toolbar) {
            super(toolbar);
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

    static class SearchTool extends Tool {
        public SearchTool(Toolbar t, Visual.Footprint f) { super(t,f);}
        @Override
        protected void onClick() {
            if (!toolbar.examining) {
                GameScene.selectCell(toolbar.informer);
                toolbar.examining = true;
            } else {
                toolbar.informer.onSelect(null);
                Dungeon.hero.search(true);
            }
        }

        @Override
        public GameAction keyAction() {
            return SPDAction.SEARCH;
        }

        @Override
        protected boolean onLongClick() {
            Dungeon.hero.search(true);
            return true;
        }
    }


    static class InventoryTool extends Tool {
        public InventoryTool(Toolbar t,Visual.Footprint f) { super(t, f);}

        private GoldIndicator gold;

        @Override
        protected void onClick() {
            GameScene.show(new WndBag(Dungeon.hero.belongings.backpack, null, WndBag.Mode.ALL, null));
        }

        @Override
        public GameAction keyAction() {
            return SPDAction.INVENTORY;
        }

        @Override
        protected boolean onLongClick() {
            WndJournal.last_index = 3; //catalog page
            GameScene.show(new WndJournal());
            return true;
        }

        @Override
        protected void createChildren() {
            super.createChildren();
            gold = new GoldIndicator();
            add(gold);
        }

        @Override
        protected void layout() {
            super.layout();
            gold.fill(this);
        }
    }
}
