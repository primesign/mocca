/*
 * Copyright 2011 by Graz University of Technology, Austria
 * MOCCA has been developed by the E-Government Innovation Center EGIZ, a joint
 * initiative of the Federal Chancellery Austria and Graz University of Technology.
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * http://www.osor.eu/eupl/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This product combines work with different licenses. See the "NOTICE" text
 * file for details on the various modules and licenses.
 * The "NOTICE" text file is part of the distribution. Any derivative works
 * that you distribute must include a readable copy of the "NOTICE" text file.
 */


package at.gv.egiz.bku.slcommands.impl.xsect;

import iaik.xml.crypto.dsig.TransformImpl;
import iaik.xml.crypto.dsig.TransformsImpl;

import java.util.List;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMCryptoContext;
import javax.xml.crypto.dsig.Transform;

import org.w3c.dom.Node;

/**
 * This class extends the XSECT TransformsImpl to allow for the use of an
 * unmarshalled <code>ds:Transforms</code> element for initalization.
 * 
 * @author mcentner
 */
public class XSECTTransforms extends TransformsImpl {
 
  /**
   * Creates a new XSECTTransforms with the given list of <code>transforms</code>.
   * 
   * @param transforms a list of {@link TransformImpl}s
   * @see TransformsImpl#TransformsImpl(List)
   */
  public XSECTTransforms(List<Transform> transforms) {
    super(transforms);
  }

  /**
   * Creates a new XSECTTransforms and initializes it from the given
   * <code>ds:Transforms</code> node.
   * 
   * @param context the context used for unmarshalling
   * @param node the <code>ds:Transforms</code> node
   * 
   * @throws MarshalException if unmarshalling the <code>ds:Transforms</code> fails
   */
  public XSECTTransforms(DOMCryptoContext context, Node node)
      throws MarshalException {
    super(context, node);
  }

  /**
   * Inserts the given <code>transform</code> at the top of the
   * transform list.
   * 
   * @param transform the <code>ds:Transform</code> to instert
   */
  @SuppressWarnings("unchecked")
  public void insertTransform(Transform transform) {
    if (transform == null) {
      throw new NullPointerException("Parameter 'transform' must not be null.");
    }
    if (!(transform instanceof TransformImpl)) {
      throw new ClassCastException("Transform 'transform' must be of type '" + TransformImpl.class.getName() + "'.");
    }
    transforms_.add(0, transform);
  }
  
  /**
   * @return
   */
  @SuppressWarnings("unchecked")
  private List<TransformImpl> getTransformImpls() {
    return transforms_;
  }

  /* (non-Javadoc)
   * @see iaik.xml.crypto.dsig.TransformsType#marshal(javax.xml.crypto.dom.DOMCryptoContext, org.w3c.dom.Node, org.w3c.dom.Node)
   */
  @Override
  public Node marshal(DOMCryptoContext context, Node parent, Node nextSibling)
      throws MarshalException {

    if (getNode() != null) {
      // If this TransformsImpl has been unmarshalled from exiting nodes,
      // we don't want to re-marshal ...
      state_ = STATE_MARSHALED;
      
      // ... but append the existing node to the parent ...
      Node transformsNode = parent.insertBefore(getNode(), nextSibling);
      
      // ... and marshal any Transforms not yet marshalled (e.g. that
      // have been added via insertTransform().
      Node transformNextSibling = transformsNode.getFirstChild();
      List<TransformImpl> transforms = getTransformImpls();
      for (int i = 0; i < transforms.size(); i++) {
        TransformImpl transform = transforms.get(i);
        Node transformNode = transform.getNode();
        if (transformNode == null) {
          // marshall TransformImpl
          transformNode = transform.marshal(context, transformsNode, transformNextSibling);
        }
        transformNextSibling = transformNode.getNextSibling();
      }
      
      return transformsNode;
    } else {
      return super.marshal(context, parent, nextSibling);
    }
    
  }
  
}
