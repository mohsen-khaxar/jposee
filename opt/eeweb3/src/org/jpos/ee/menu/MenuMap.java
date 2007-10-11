package org.jpos.ee.menu;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jpos.ee.BLException;

/**
 * This class holds a map with all defined menus in a directory.<br>
 * It takes care or reading the directory and instantiating the {@link MenuNode} objects.<br>
 * A menu from an xml file can be place inside a menu from another using 
 * the xml attribute parent and path, if path is ommited it will be places as a child 
 * of the parent root. <br>
 * Example:<br> 
 * &lt;menu name="child" parent="parent-menu" path="path/to/child"><br>
 * ...items <br>
 * &lt;/menu>
 * 
 * @author alcarraz
 * @see MenuNode
 */
public class MenuMap {
    /**holds the menu map*/
    private Map<String,MenuNode> menus;
    
    /**loads all the menus from a file*/
    public void load(File dir) throws BLException{
        try {
            Map<String,MenuNode> menus = new HashMap<String, MenuNode>();
            Set<Element> elements = new HashSet<Element>();
            if (!dir.isDirectory())
                throw new BLException("path " + dir.getPath() + " is not a directory");
            for (File f : dir.listFiles()) {
                if (f.getName().endsWith(".xml")) {
                    SAXBuilder builder = new SAXBuilder();
                    Document d = builder.build(f);
                    Element e =d.getRootElement(); 
                    MenuNode mn = new MenuNode();
                    mn.build(e);
                    elements.add(e);
                    menus.put(mn.getName(), mn);
                }
            }
            //second pass to set parents
            for (Element e : elements){
                String parentName = e.getAttributeValue("parent");
                if (parentName != null){
                    //FIXME by now assuming that top most item isn't part of the hierarchy
                    //is this correct?
                    MenuNode child = menus.get(e.getAttributeValue("name"));
                    MenuNode parent = menus.get(parentName);
                    if (parent == null) {
                        throw new BLException("parent menu " + parentName + " does not exists");
                    } else {
                        String path = e.getAttributeValue("path");
                        if (path != null)
                            parent = parent.getNodeByPath(path);
                        for (MenuNode n : child) 
                            parent.addChild(n);
                    }
                }
                    
            }
            this.menus = menus;
        } catch (JDOMException e) {
            throw new BLException("Error reading menus", e);
        } catch (IOException e) {
            throw new BLException("Error reading menus", e);
        }
    }
    
    /**retrieves the menu given by name*/
    public MenuNode getMenu (String name){
        return menus.get(name);
    }
}
