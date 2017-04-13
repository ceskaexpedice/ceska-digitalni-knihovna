/*
 * Copyright (C) 2016 Pavel Stastny
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.incad.cdk.cdkharvester.foxmlprocess;

import java.io.InputStream;

/**
 * The FOXML post processing chain
 * @author pavels
 */
public interface ProcessFOXML {
    
    /**
     * Postprocessing method
     * @param pid Pid 
     * @param is inputStream
     */
    public byte[] process(String url, String pid, InputStream is) throws Exception;
}
