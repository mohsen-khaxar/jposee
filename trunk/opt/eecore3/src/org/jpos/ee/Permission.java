/*
 *  jPOS Extended Edition
 *  Copyright (C) 2004 Alejandro P. Revilla
 *  jPOS.org (http://jpos.org)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jpos.ee;

import java.io.Serializable;

public class Permission implements Serializable, Constants {
    String name;
    public Permission () {
        super ();
        setName ("");
    }
    public Permission (String name) {
        super ();
        setName (name);
    }
    public void setName (String name) {
        this.name = name;
    }
    public String getName () {
        return name;
    }
    public boolean equals (Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj instanceof Permission) {
            return this.getName().equals (((Permission)obj).getName());
	}
	return false;
    }
    public String toString () {
        return getName ();
    }
    public int hashCode() {
        return name.hashCode ();
    }
}

