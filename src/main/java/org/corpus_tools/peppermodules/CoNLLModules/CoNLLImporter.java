/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.CoNLLModules;

import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.peppermodules.conll.Conll2SaltMapper;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

@Component(name = "CoNLLImporterComponent", factory = "PepperImporterComponentFactory")
public class CoNLLImporter extends PepperImporterImpl implements PepperImporter {
	// -------------------------------------------------------------------------
	public static final String NAME = "CoNLLImporter";
	public static final String FORMATNAME = "CoNLL";
	public static final String FORMATVERSION = "1.0"; // TODO: What version?
	// -------------------------------------------------------------------------

	public CoNLLImporter() {
		super();
		// setting name of module
		this.setName(NAME);
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-CoNLLModules"));
		setDesc("This importer transforms data in CoNLL format to a Salt model. ");
		// set list of formats supported by this module
		this.addSupportedFormat(FORMATNAME, FORMATVERSION, null);
		getDocumentEndings().add(PepperImporter.ENDING_ALL_FILES);
		setProperties(new CoNLLImporterProperties());
	}

	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		Conll2SaltMapper mapper = new Conll2SaltMapper();
		return (mapper);
	}
}
