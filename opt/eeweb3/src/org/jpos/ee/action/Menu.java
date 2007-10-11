package org.jpos.ee.action;

import java.io.File;

import org.jpos.ee.BLException;
import org.jpos.ee.User;
import org.jpos.ee.menu.MenuMap;
import org.jpos.ee.menu.MenuNode;
import org.jpos.space.Space;
import org.jpos.space.TinySpace;
import org.jpublish.JPublishContext;

import com.anthonyeden.lib.config.Configuration;

/**
 * Action that loads the user menu.
 * @author alcarraz
 * @FIXME this action could be integrated with open 
 */

public class Menu extends ActionSupport {
    

    public void execute(JPublishContext context, Configuration cfg) {
        try {
            User u = getUser(context);
            
            MenuNode m;
            MenuMap mm = getMenuMap(context, cfg);
            if (u == null || context.getPage().getPath().endsWith("logout.html")) { 
                m = mm.getMenu(cfg.getAttribute("nologged-menu","nologged"));
            } else {
                Space sp = (Space)context.getSession().getAttribute(MENU);
                if (sp == null) 
                    context.getSession().setAttribute(MENU, sp = new TinySpace());
                m = (MenuNode)sp.rdp(MENU);
                if (m==null) {
                    m = mm.getMenu("main").prune(u);
                    sp.out(MENU, m, Long.parseLong(cfg.getAttribute("menu-reload-time", "300000")));
                    context.getSession().setAttribute(MENU, sp);
                }
            }
            context.put(MENU,m);
        } catch (BLException e) {
            context.getSyslog().error (e);
        }
    }

    private MenuMap getMenuMap(JPublishContext context, Configuration cfg) throws BLException {
        //it is not traumatic to load the menu map twice if the first two 
        //requests come together, anyway this code could be in an application 
        //startup action
        MenuMap mm = (MenuMap)context.getApplication().getAttribute(MENU_MAP);
        if (mm==null) {
            mm = new MenuMap();
            mm.load(new File(cfg.getAttribute("directory", "cfg/menus")));
            context.getApplication().setAttribute(MENU_MAP, mm);
        }
        return mm;
        
    }

     
}
