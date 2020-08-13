package com.shatteredpixel.shatteredpixeldungeon.ui;

import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDAction;
import com.watabou.input.GameAction;
import com.watabou.noosa.ui.Button;

/** InvisibleTool has no Image/frame/children */
class InvisibleTool extends Button {
    protected Toolbar toolbar;

    public InvisibleTool(Toolbar toolbar) {
        this.toolbar = toolbar;
    }

    static class RestButton extends InvisibleTool {
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
}
