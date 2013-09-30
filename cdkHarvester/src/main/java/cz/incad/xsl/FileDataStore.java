/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.incad.xsl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import javax.imageio.ImageIO;


public class FileDataStore {

    public static String getDimensions(String pid) {

        try {

            final String path = System.getProperty("user.home") + "/.cdk/CDK_DATA/thumbs/";
            URL url = new URL(pid);
            String host = url.getHost();

            File directory = new File(path + host);
            directory.mkdirs();


            String query = url.getQuery();
            String identifier = query.substring(query.indexOf("uuid:") + 5);
            //String file_path = host + "/" + uuid.substring(0,1);

            File file = directory;
            file = new File(file, identifier.substring(0, 2));
            file = new File(file, identifier.substring(2, 4));
            file = new File(file, identifier.substring(4, 6));
            file = new File(file, identifier);


            if (!file.exists()) {
                File parent = file.getParentFile();
                parent.mkdirs();

                ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
                fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            }
//        String filename =  file.getAbsolutePath();


            //     File file = new File(filename);

            BufferedImage img = ImageIO.read(file);
            return img.getWidth() + "," + img.getHeight();
        } catch (Exception ex) {
            ex.printStackTrace();
            return "0,0";
        }
    }
}