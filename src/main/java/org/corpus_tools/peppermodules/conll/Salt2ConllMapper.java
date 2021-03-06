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
package org.corpus_tools.peppermodules.conll;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleException;
import org.corpus_tools.peppermodules.CoNLLModules.CoNLLExporterProperties;
import org.corpus_tools.peppermodules.CoNLLModules.CoNLLImporter;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleConnectorFactory;
import org.corpus_tools.peppermodules.conll.tupleconnector.TupleWriter;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;

/**
 * 
 * 
 * @author Martin Klotz
 * 
 *
 */
public class Salt2ConllMapper extends PepperMapperImpl implements PepperMapper {
	
	private static final String ERR_MSG_NO_DOCUMENT = "No document to convert.";
	private static final String ERR_MSG_EMPTY_DOCUMENT = "Document is empty.";
	private static final String NO_VALUE = "_";
	private static final String ERR_MSG_ADD_TUPLE = "There was an error adding a tuple to the tuplewriter.";
	private static final String ERR_MSG_OUTPUT_FILE = "An error occured creating the output file";
	private static final String ERR_MSG_WRONG_COLUMN_FORMAT = "The column configuration provided in the job configuration does not match."+
																" Make sure you use the right number of columns and you mark collapsing correctly.";
	
	/*properties*/
	private String dependencyQName = null;
	private String lemmaQName = null;
	private String posQName = null;
	private String cposQName = null;
	private String featsQName = null;
	private String miscQName = null;
	private String tokinfoQName = null;
	private Set<String> spanAnnos = null;
	
	private SDocumentGraph docGraph = null;
	
	@Override
	public DOCUMENT_STATUS mapSDocument(){		
		if (getDocument()==null){
			throw new PepperModuleDataException(this, ERR_MSG_NO_DOCUMENT);
		}
		docGraph = getDocument().getDocumentGraph();
		if (docGraph==null){
			throw new PepperModuleDataException(this, ERR_MSG_EMPTY_DOCUMENT);
		}
		
		readProperties();
		
		TupleWriter tw = TupleConnectorFactory.fINSTANCE.createTupleWriter();
		
		String dirname = getResourceURI().toFileString();
		(new File(dirname)).mkdirs();
		String conllFileName = dirname+getDocument().getName()+"."+CoNLLImporter.ENDING_TXT;
		File file = new File(conllFileName);
		try {
			if (!file.exists()){
				file.createNewFile();
			}
		} catch (IOException e1) {
			throw new PepperModuleException(this, ERR_MSG_OUTPUT_FILE);
		}
		tw.setFile(file);
		Collection<String> tuple = null;
		boolean isDependency;
		SAnnotation anno = null;
		boolean[] onSpan = {this.spanAnnos.contains(lemmaQName),
							this.spanAnnos.contains(cposQName),
							this.spanAnnos.contains(posQName),
							this.spanAnnos.contains(featsQName),
							this.spanAnnos.contains(miscQName),
							this.spanAnnos.contains(tokinfoQName)};
		
		boolean searchSpan = onSpan[0] || onSpan[1] || onSpan[2] || onSpan[3] || onSpan[4] || onSpan[5];
		
		for (SToken tok : docGraph.getSortedTokenByText()){
			tuple = new ArrayList<String>();
			
			tuple.add(tok.getName().replaceAll("[^0-9]", ""));//ID
			
			tuple.add(docGraph.getText(tok));//FORM
			
			SSpan span = null;
			if (!lemmaQName.trim().isEmpty()) {//LEMMA				
				if (searchSpan && onSpan[0]){
					span = getAnnotatedSpan(tok, lemmaQName);
					if (span != null){
						anno = span.getAnnotation(lemmaQName);
					}
				} else {
					anno = tok.getAnnotation(lemmaQName);
				}				
				if (anno == null || NO_VALUE.equals(lemmaQName)){
					tuple.add(NO_VALUE);
				} else {
					tuple.add(anno.getValue().toString());
				}
			}

			anno = null;			
			if (!cposQName.trim().isEmpty()) {//CPOS				
				if (searchSpan && onSpan[1]){
					span = getAnnotatedSpan(tok, cposQName);
					if (span != null){
						anno = span.getAnnotation(cposQName);
					}					
				} else {
					anno = tok.getAnnotation(cposQName);
				}
				if (anno == null || NO_VALUE.equals(cposQName)){
					tuple.add(NO_VALUE);
				} else {
					tuple.add(anno.getValue().toString());
				}
			}

			anno = null;
			if (!posQName.trim().isEmpty()) {//POS
				if (searchSpan && onSpan[2]){
					span = getAnnotatedSpan(tok, posQName);
					if (span != null){
						anno = span.getAnnotation(posQName);
					}					
				} else {
					anno = tok.getAnnotation(posQName);
				}
				if (anno == null || NO_VALUE.equals(posQName)){
					tuple.add(NO_VALUE);
				} else {
					tuple.add(anno.getValue().toString());
				}
			}
			
			anno = null;
			if (!featsQName.trim().isEmpty()) {//FEATS
				if (searchSpan && onSpan[3]){
					span = getAnnotatedSpan(tok, featsQName);
					if (span != null){
						anno = span.getAnnotation(featsQName);
					}					
				} else {
					anno = tok.getAnnotation(featsQName);
				}
				if (anno == null || NO_VALUE.equals(featsQName)){
					tuple.add(NO_VALUE);
				} else {
					tuple.add(anno.getValue().toString());
				}
			}
			
			anno = null;
			if (!dependencyQName.trim().isEmpty()){//DEPENDENCY
				Collection<SRelation> incoming = tok.getInRelations();
				isDependency = false;
				SRelation next = null;
				anno = null;
				if (!incoming.isEmpty()){					
					for (Iterator<SRelation> iter=incoming.iterator();
							iter.hasNext()&&!isDependency;
							next=iter.next(), isDependency=next.getAnnotation(dependencyQName)!=null, anno=next.getAnnotation(dependencyQName)){}				
					tuple.add(isDependency? ((SToken)next.getSource()).getName().replaceAll("[^0-9]", "") : NO_VALUE);//HEAD
					tuple.add(anno==null? NO_VALUE : (anno.getValue()==null? NO_VALUE : anno.getValue_STEXT()));//FUNC
				}						
			}
			
			anno = null;
			if (!miscQName.trim().isEmpty()) {//MISC
				if (searchSpan && onSpan[4]){
					span = getAnnotatedSpan(tok, miscQName);
					if (span != null){
						anno = span.getAnnotation(miscQName);
					}					
				} else {
					anno = tok.getAnnotation(miscQName);
				}
				if (anno == null || NO_VALUE.equals(miscQName)){
					tuple.add(NO_VALUE);
				} else {
					tuple.add(anno.getValue().toString());
				}
			}
			
			anno = null;
			if (!tokinfoQName.trim().isEmpty()) {//TOK_INFO
				if (searchSpan && onSpan[5]){
					span = getAnnotatedSpan(tok, tokinfoQName);
					if (span != null){
						anno = span.getAnnotation(tokinfoQName);
					}					
				} else {
					anno = tok.getAnnotation(tokinfoQName);
				}
				if (anno == null || NO_VALUE.equals(tokinfoQName)){
					tuple.add(NO_VALUE);
				} else {
					tuple.add(anno.getValue().toString());
				}
			}
			
			try {
				tw.addTuple(tuple);
			} catch (FileNotFoundException e) {
				throw new PepperModuleException(this, ERR_MSG_ADD_TUPLE);
			}
		}		
		
		return DOCUMENT_STATUS.COMPLETED;
	}
	
	private SSpan getAnnotatedSpan(SToken tok, String annoQName){
		SSpan span = null;
		SAnnotation anno = null;
		Iterator<SRelation> itRels = tok.getInRelations().iterator();					
		while (anno == null && itRels.hasNext()){
			SRelation nxtRel = itRels.next();
			if (nxtRel instanceof SSpanningRelation){
				span = (SSpan)nxtRel.getSource();
				anno = span.getAnnotation(annoQName);
			}						
		}
		return anno == null? null : span;
	}

	private void readProperties(){
		CoNLLExporterProperties properties = (CoNLLExporterProperties)this.getProperties();
		Map<ConllDataField, String> columns = properties.getColumns();
		if (columns == null){
			throw new PepperModuleException(ERR_MSG_WRONG_COLUMN_FORMAT);
		}
		this.dependencyQName = columns.get(ConllDataField.DEPREL);
		this.lemmaQName = columns.get(ConllDataField.LEMMA);
		this.posQName = columns.get(ConllDataField.POSTAG);
		this.cposQName = columns.get(ConllDataField.CPOSTAG);
		this.featsQName = columns.get(ConllDataField.FEATS);
		this.miscQName = columns.get(ConllDataField.PHEAD);
		this.tokinfoQName = columns.get(ConllDataField.PDEPREL);
		
		this.spanAnnos = properties.getSpanAnnotations();
	}
}
