package nc.noumea.mairie.kiosque.export;

/*
 * #%L
 * sirh-kiosque-j2ee
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2014 Mairie de Nouméa
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import static nc.noumea.mairie.kiosque.export.Utils.getAlign;
import static nc.noumea.mairie.kiosque.export.Utils.getFooterColumnHeader;
import static nc.noumea.mairie.kiosque.export.Utils.getFooters;
import static nc.noumea.mairie.kiosque.export.Utils.getHeaderSize;
import static nc.noumea.mairie.kiosque.export.Utils.getHeaders;
import static nc.noumea.mairie.kiosque.export.Utils.getStringValue;
import static nc.noumea.mairie.kiosque.export.Utils.getTarget;
import static nc.noumea.mairie.kiosque.export.Utils.invokeComponentGetter;

import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import nc.noumea.mairie.kiosque.export.pdf.DocumentFactory;
import nc.noumea.mairie.kiosque.export.pdf.DocumentFactoryImpl;
import nc.noumea.mairie.kiosque.export.pdf.FontFactory;
import nc.noumea.mairie.kiosque.export.pdf.FontFactoryImpl;
import nc.noumea.mairie.kiosque.export.pdf.PdfPCellFactory;
import nc.noumea.mairie.kiosque.export.pdf.PdfPCellFactoryImpl;
import nc.noumea.mairie.kiosque.export.pdf.PdfPTableFactory;
import nc.noumea.mairie.kiosque.export.pdf.PdfPTableFactoryImpl;
import nc.noumea.mairie.kiosque.export.pdf.PdfWriterFactory;
import nc.noumea.mairie.kiosque.export.pdf.PdfWriterFactoryImpl;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.HeaderElement;
import org.zkoss.zul.impl.MeshElement;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

/**
 * @author Sam
 * 
 */
public class PdfExporter extends AbstractExporter<PdfPTable, PdfPTable> {
	private DocumentFactory _documentFactory;
	private PdfWriterFactory _pdfWriterFactory;
	private FontFactory _fontFactory;
	private PdfPTableFactory _pdfPTableFactory;
	private PdfPCellFactory _pdfPCellFactory;

	public <D> void export(int columnSize, Collection<D> data, RowRenderer<PdfPTable, D> renderer, OutputStream outputStream) throws Exception {
		Document document = getDocumentFactory().getDocument();
		getPdfWriterFactory().getPdfWriter(document, outputStream);
		document.open();
		PdfPTable pdfPTable = getPdfPTableFactory().getPdfPTable(columnSize);
		getInterceptor();
		if (getInterceptor() != null)
			getInterceptor().beforeRendering(pdfPTable);
		int rowIndex = 0;
		for (D d : data) {
			renderer.render(pdfPTable, d, (rowIndex++ % 2 == 1));
		}
		if (getInterceptor() != null)
			getInterceptor().afterRendering(pdfPTable);
		document.add(pdfPTable);
		document.close();
	}

	public <T> void export(final String[] columnHeaders, final Collection<T> data, RowRenderer<PdfPTable, T> renderer, OutputStream outputStream) throws Exception {
		final int columnSize = columnHeaders.length;
		if (getInterceptor() == null)
			setInterceptor(new ExportColumnHeaderInterceptorImpl(columnHeaders));
		export(columnSize, data, renderer, outputStream);
	}

	public <D> void export(int columnSize, final Collection<Collection<D>> data, GroupRenderer<PdfPTable, D> renderer, OutputStream outputStream) throws Exception {
		Document document = getDocumentFactory().getDocument();
		getPdfWriterFactory().getPdfWriter(document, outputStream);
		document.open();
		PdfPTable table = getPdfPTableFactory().getPdfPTable(columnSize);
		if (getInterceptor() != null)
			getInterceptor().beforeRendering(table);
		int rowIndex = 0;
		for (Collection<D> group : data) {
			renderer.renderGroup(table, group);
			for (D d : group) {
				renderer.render(table, d, (rowIndex++ % 2 == 1));
			}
			renderer.renderGroupfoot(table, group);
		}
		if (getInterceptor() != null)
			getInterceptor().afterRendering(table);
		document.add(table);
		document.close();
	}

	public <T> void export(final String[] columnHeaders, final Collection<Collection<T>> data, GroupRenderer<PdfPTable, T> renderer, OutputStream outputStream) throws Exception {
		final int columnSize = columnHeaders.length;
		if (getInterceptor() == null)
			setInterceptor(new ExportColumnHeaderInterceptorImpl(columnHeaders));
		export(columnSize, data, renderer, outputStream);
	}

	public void exportTabularComponent(MeshElement component, OutputStream outputStream) throws Exception {
		Document document = getDocumentFactory().getDocument();
		getPdfWriterFactory().getPdfWriter(document, outputStream);
		document.open();
		int columnSize = getHeaderSize(component);
		PdfPTable table = getPdfPTableFactory().getPdfPTable(columnSize);
		if (getInterceptor() != null)
			getInterceptor().beforeRendering(table);
		exportHeaders(columnSize, component, table);
		exportRows(columnSize, component, table);
		exportFooters(columnSize, component, table);
		if (getInterceptor() != null)
			getInterceptor().afterRendering(table);
		document.add(table);
		document.close();
	}

	protected void exportFooters(int columnSize, Component target, PdfPTable table) {
		Component footers = getFooters(target);
		if (footers == null) {
			return;
		}
		List<Component> children = target.getChildren();
		int colSpan = 0;
		for (int i = 0; i < columnSize; i++) {
			Component current = i < children.size() ? children.get(i) : null;
			Component next = i + 1 < children.size() ? children.get(i + 1) : null;
			if (current == null)
				break;
			PdfPCell cell = getPdfPCellFactory().getFooterCell();
			cell.setPhrase(new Phrase(getStringValue(current), getFontFactory().getFont(FontFactory.FONT_TYPE_FOOTER)));
			syncAlignment(getFooterColumnHeader(current), cell);
			if (next == null && colSpan < columnSize - 1) {
				cell.setBorderWidthRight(0);
			} else {
				colSpan += syncCellColSpan(current, cell);
			}
			table.addCell(cell);
			if (next == null && colSpan < columnSize - 1) {
				cell = getPdfPCellFactory().getFooterCell();
				cell.setBorderWidthLeft(0);
				cell.setColspan(columnSize - colSpan - 1);
				table.addCell(cell);
			}
		}
		table.completeRow();
	}

	private HashMap<Integer, Component> buildHeaderIndexMap(Component target) {
		if (target == null)
			return null;
		HashMap<Integer, Component> headers = new HashMap<Integer, Component>();
		int idx = 0;
		for (Component c : target.getChildren()) {
			if (!(c instanceof HeaderElement)) {
				throw new IllegalArgumentException(c + " is not type of HeaderElement");
			}
			headers.put(idx++, c);
		}
		return headers;
	}

	protected void exportCells(int rowIndex, int columnSize, Component row, PdfPTable table) {
		HashMap<Integer, Component> headers = buildHeaderIndexMap(getHeaders(getTarget(row)));
		List<Component> children = row.getChildren();
		for (int c = 0; c < columnSize; c++) {
			Component cmp = c < children.size() ? children.get(c) : null;
			if (cmp == null) {
				PdfPCell cell = getPdfPCellFactory().getCell(false);
				cell.setColspan(columnSize - c);
				table.addCell(cell);
				return;
			}

			String vlayout = "";
			if (cmp instanceof Vlayout) {
				Iterator<Component> iterator = cmp.getChildren().iterator();
				while (iterator.hasNext()) {
					cmp = iterator.next();
					vlayout += getStringValue(cmp) == null ? "" : getStringValue(cmp);
				}
			}
			PdfPCell cell = getPdfPCellFactory().getCell(isOddRow(rowIndex));
			cell.setPhrase(new Phrase(vlayout.equals("") ? getStringValue(cmp) : vlayout, getFontFactory().getFont(FontFactory.FONT_TYPE_CELL)));
			syncCellColSpan(cmp, cell);
			syncAlignment(cmp, headers != null ? headers.get(c) : null, cell);
			table.addCell(cell);

		}
		table.completeRow();

	}

	private boolean syncAlignment(Component cmp, PdfPCell cell) {
		if (cmp == null)
			return false;
		final String align = getAlign(cmp);
		if ("center".equals(align)) {
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			return true;
		} else if ("right".equals(align)) {
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			return true;
		} else if ("left".equals(align)) {
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			return true;
		}
		return false;
	}

	protected void exportAuxhead(int columnSize, Auxhead auxhead, PdfPTable table) {
		List<Component> children = auxhead.getChildren();
		int colSpan = 0;
		for (int i = 0; i < columnSize; i++) {
			Auxheader header = i < children.size() ? (Auxheader) children.get(i) : null;
			Auxheader next = i + 1 < children.size() ? (Auxheader) children.get(i + 1) : null;
			if (header == null)
				break;
			PdfPCell cell = getPdfPCellFactory().getHeaderCell();
			cell.setPhrase(new Phrase(header.getLabel(), getFontFactory().getFont(FontFactory.FONT_TYPE_HEADER)));
			if (next == null && colSpan < columnSize - 1) {
				cell.setBorderWidthRight(0);
			} else {
				colSpan += syncCellColSpan(header, cell);
			}
			table.addCell(cell);
			if (next == null && colSpan < columnSize - 1) {
				cell = getPdfPCellFactory().getHeaderCell();
				cell.setBorderWidthLeft(0);
				cell.setColspan(columnSize - colSpan - 1);
				table.addCell(cell);
			}
		}
		table.completeRow();
	}

	protected void exportColumnHeaders(Component cmp, PdfPTable table) {
		for (Component c : cmp.getChildren()) {
			String label = getStringValue(c);
			PdfPCell cell = getPdfPCellFactory().getHeaderCell();
			cell.setPhrase(new Phrase(label, getFontFactory().getFont(FontFactory.FONT_TYPE_HEADER)));
			syncAlignment(c, cell);
			table.addCell(cell);
		}
		table.completeRow();
	}

	protected void exportGroupfoot(int columnSize, Component groupfoot, PdfPTable table) {
		List<Component> children = groupfoot.getChildren();
		for (int c = 0; c < columnSize; c++) {
			Component cmp = c < children.size() ? children.get(c) : null;
			if (cmp == null) {
				PdfPCell cell = getPdfPCellFactory().getCell(false);
				cell.setColspan(columnSize - c);
				table.addCell(cell);
				return;
			}
			PdfPCell cell = getPdfPCellFactory().getGroupfootCell();
			cell.setPhrase(new Phrase(getStringValue(cmp), getFontFactory().getFont(FontFactory.FONT_TYPE_GROUPFOOT)));
			table.addCell(cell);
		}
		table.completeRow();
	}

	protected void exportGroup(int columnSize, Component group, PdfPTable table) {
		Iterator<Component> iterator = group.getChildren().iterator();
		while (iterator.hasNext()) {
			Component cmp = iterator.next();
			PdfPCell cell = getPdfPCellFactory().getGroupCell();
			cell.setPhrase(new Phrase(getStringValue(cmp), getFontFactory().getFont(FontFactory.FONT_TYPE_GROUP)));
			cell.setColspan(columnSize);
			table.addCell(cell);
		}
		table.completeRow();
	}

	private void syncAlignment(Component cmp, Component header, PdfPCell cell) {
		// check if component define align, if not check header's align
		if (!syncAlignment(cmp, cell) && header != null) {
			syncAlignment(header, cell);
		}
	}

	@SuppressWarnings("unused")
	private void syncCellRowSpan(Component cmp, PdfPCell cell) {
		Object rowSpan = invokeComponentGetter(cmp, "getRowspan");
		if (rowSpan instanceof Number) {
			cell.setRowspan(((Number) rowSpan).intValue());
		}
	}

	private int syncCellColSpan(Component cmp, PdfPCell cell) {
		int span = 1;
		Object spanVal = invokeComponentGetter(cmp, "getColspan", "getSpan");
		if (spanVal != null && spanVal instanceof Number)
			span = ((Number) spanVal).intValue();
		return span;
	}

	private boolean isOddRow(int rowIndex) {
		return rowIndex % 2 == 1;
	}

	public PdfExporter setDocumentFactory(DocumentFactory documentFactory) {
		_documentFactory = documentFactory;
		return this;
	}

	public DocumentFactory getDocumentFactory() {
		if (_documentFactory == null) {
			_documentFactory = new DocumentFactoryImpl();
		}
		return _documentFactory;
	}

	public PdfExporter setPdfWriterFactory(PdfWriterFactory pdfWriterFactory) {
		_pdfWriterFactory = pdfWriterFactory;
		return this;
	}

	public PdfWriterFactory getPdfWriterFactory() {
		if (_pdfWriterFactory == null) {
			_pdfWriterFactory = new PdfWriterFactoryImpl();
		}
		return _pdfWriterFactory;
	}

	public PdfExporter setPdfPTableFactory(PdfPTableFactory pdfPTableFactory) {
		_pdfPTableFactory = pdfPTableFactory;
		return this;
	}

	public PdfPTableFactory getPdfPTableFactory() {
		if (_pdfPTableFactory == null) {
			_pdfPTableFactory = new PdfPTableFactoryImpl();
		}
		return _pdfPTableFactory;
	}

	public PdfExporter setFontFactory(FontFactory fontFactory) {
		_fontFactory = fontFactory;
		return this;
	}

	public FontFactory getFontFactory() {
		if (_fontFactory == null) {
			_fontFactory = new FontFactoryImpl();
		}
		return _fontFactory;
	}

	public PdfExporter setdfPCellFactory(PdfPCellFactory pdfPCellFactory) {
		_pdfPCellFactory = pdfPCellFactory;
		return this;
	}

	public PdfPCellFactory getPdfPCellFactory() {
		if (_pdfPCellFactory == null) {
			_pdfPCellFactory = new PdfPCellFactoryImpl();
		}
		return _pdfPCellFactory;
	}

	// export header
	private class ExportColumnHeaderInterceptorImpl implements Interceptor<PdfPTable> {
		private final String[] _columnHeaders;

		public ExportColumnHeaderInterceptorImpl(String[] columnHeaders) {
			_columnHeaders = columnHeaders;
		}

		@Override
		public void beforeRendering(PdfPTable table) {
			int columnSize = _columnHeaders.length;
			boolean renderHeader = false;
			for (int i = 0; i < columnSize; i++) {
				String e = _columnHeaders[i];
				if (e != null && e.length() > 0) {
					renderHeader = true;
					break;
				}
			}
			if (renderHeader) {
				for (int i = 0; i < _columnHeaders.length; i++) {
					String header = _columnHeaders[i];
					Font font = getFontFactory().getFont(FontFactory.FONT_TYPE_HEADER);
					PdfPCell headerCell = getPdfPCellFactory().getHeaderCell();
					headerCell.setPhrase(new Phrase(header, font));
					table.addCell(headerCell);
				}
				table.completeRow();
			}
		}

		@Override
		public void afterRendering(PdfPTable table) {
		}
	}
}