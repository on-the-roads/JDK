/*
 * Copyright (c) 2001, 2018, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package jdk.javadoc.internal.doclets.toolkit.taglets;

import java.util.EnumSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

import com.sun.source.doctree.DocTree;
import jdk.javadoc.internal.doclets.toolkit.BaseConfiguration;
import jdk.javadoc.internal.doclets.toolkit.Content;
import jdk.javadoc.internal.doclets.toolkit.Messages;
import jdk.javadoc.internal.doclets.toolkit.util.CommentHelper;
import jdk.javadoc.internal.doclets.toolkit.util.DocFinder;
import jdk.javadoc.internal.doclets.toolkit.util.DocFinder.Input;
import jdk.javadoc.internal.doclets.toolkit.util.Utils;

import static com.sun.source.doctree.DocTree.Kind.INHERIT_DOC;

/**
 * An inline Taglet representing the {@code inheritDoc} tag. This tag should only
 * be used with a method.  It is used to inherit documentation from overriden
 * and implemented methods.
 *
 *  <p><b>This is NOT part of any supported API.
 *  If you write code that depends on this, you do so at your own risk.
 *  This code and its internal interfaces are subject to change or
 *  deletion without notice.</b>
 *
 * @author Jamie Ho
 */

public class InheritDocTaglet extends BaseTaglet {

    /**
     * Construct a new InheritDocTaglet.
     */
    public InheritDocTaglet () {
        super(INHERIT_DOC.tagName, true, EnumSet.of(Site.TYPE, Site.METHOD));
    }

    /**
     * Given an element, a {@code DocTree} in the element's doc comment
     * replace all occurrences of @inheritDoc with documentation from its
     * superclass or superinterface.
     *
     * @param writer the writer that is writing the output.
     * @param e the {@link Element} that we are documenting.
     * @param holderTag the tag that holds the inheritDoc tag or null for type
     * (class) docs.
     * @param isFirstSentence true if we only want to inherit the first sentence.
     */
    private Content retrieveInheritedDocumentation(TagletWriter writer,
            Element e, DocTree holderTag, boolean isFirstSentence) {
        Content replacement = writer.getOutputInstance();
        BaseConfiguration configuration = writer.configuration();
        Messages messages = configuration.getMessages();
        Utils utils = configuration.utils;
        CommentHelper ch = utils.getCommentHelper(e);
        Taglet inheritableTaglet = holderTag == null
                ? null
                : configuration.tagletManager.getTaglet(ch.getTagName(holderTag));
        if (inheritableTaglet != null &&
            !(inheritableTaglet instanceof InheritableTaglet)) {
                String message = utils.getSimpleName(e) +
                    ((utils.isExecutableElement(e))
                        ? utils.flatSignature((ExecutableElement)e)
                        : "");
                //This tag does not support inheritance.
                messages.warning(e, "doclet.noInheritedDoc", message);
        }
        Input input = new DocFinder.Input(utils, e,
                (InheritableTaglet) inheritableTaglet, new DocFinder.DocTreeInfo(holderTag, e),
                isFirstSentence, true);
        DocFinder.Output inheritedDoc = DocFinder.search(configuration, input);
        if (inheritedDoc.isValidInheritDocTag) {
            if (!inheritedDoc.inlineTags.isEmpty()) {
                replacement = writer.commentTagsToOutput(inheritedDoc.holderTag,
                    inheritedDoc.holder, inheritedDoc.inlineTags, isFirstSentence);
                ch.setOverrideElement(inheritedDoc.holder);
            }

        } else {
            String message = utils.getSimpleName(e) +
                    ((utils.isExecutableElement(e))
                        ? utils.flatSignature((ExecutableElement)e)
                        : "");
            messages.warning(e, "doclet.noInheritedDoc", message);
        }
        return replacement;
    }

    @Override
    public Content getTagletOutput(Element e, DocTree tag, TagletWriter tagletWriter) {
        DocTree inheritTag = (tag.getKind() == INHERIT_DOC) ? null : tag;
        return retrieveInheritedDocumentation(tagletWriter, e,
                inheritTag, tagletWriter.isFirstSentence);
    }
}
