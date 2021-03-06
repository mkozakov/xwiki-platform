/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.rendering.internal.transformation.macro;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MetaDataBlock;
import org.xwiki.rendering.block.match.MetadataBlockMatcher;
import org.xwiki.rendering.listener.MetaData;

/**
 * Resolves a String reference, usually passed as a Macro parameter, into a {@link DocumentReference}, using any
 * {@link MetaDataBlock} with a {@link MetaData#BASE} setting to resolve the any relative reference parts into a fully
 * resolved object.
 *
 * @version $Id$
 * @since 4.3M1
 */
@Component
@Named("macro")
@Singleton
public class CurrentMacroDocumentReferenceResolver implements DocumentReferenceResolver<String>
{
    /**
     * Used to resolve references.
     */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> currentDocumentReferenceResolver;

    @Override
    public DocumentReference resolve(String documentReferenceRepresentation, Object... parameters)
    {
        // There must one parameter and it must be of type Block
        if (parameters.length != 1 || !(parameters[0] instanceof Block)) {
            throw new IllegalArgumentException(
                String.format("You must pass one parameter of type [%s]", Block.class.getName()));
        }

        Block currentBlock = (Block) parameters[0];

        DocumentReference result;

        MetaDataBlock metaDataBlock =
            currentBlock.getFirstBlock(new MetadataBlockMatcher(MetaData.BASE), Block.Axes.ANCESTOR);

        // If no Source MetaData was found resolve against the current document as a failsafe solution.
        if (metaDataBlock == null) {
            result = this.currentDocumentReferenceResolver.resolve(documentReferenceRepresentation);
        } else {
            String sourceMetaData = (String) metaDataBlock.getMetaData().getMetaData(MetaData.BASE);
            result = this.currentDocumentReferenceResolver.resolve(documentReferenceRepresentation,
                this.currentDocumentReferenceResolver.resolve(sourceMetaData));
        }

        return result;
    }
}
