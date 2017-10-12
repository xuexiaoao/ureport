/*******************************************************************************
 * Copyright 2017 Bstek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.build.paging;

import java.util.ArrayList;
import java.util.List;

import com.bstek.ureport.definition.Band;
import com.bstek.ureport.definition.Paper;
import com.bstek.ureport.model.Report;
import com.bstek.ureport.model.Row;

/**
 * @author Cooper
 * @since 2017年10月11日
 */
public class DatePagination extends BasePagination implements Pagination {
	@Override
	public List<Page> doPaging(Report report) {
		List<Row> rows=report.getRows();
		List<Row> headerRows=report.getHeaderRepeatRows();
		List<Row> footerRows=report.getFooterRepeatRows();
		List<Row> titleRows=report.getTitleRows();
		List<Row> summaryRows=report.getSummaryRows();

		List<Row> pageRepeatHeaders=new ArrayList<>();
		List<Row> pageRepeatFooters=new ArrayList<>();
		pageRepeatHeaders.addAll(headerRows);
		pageRepeatFooters.addAll(footerRows);
		List<Page> pages=new ArrayList<>();
		List<Row> pageRows=new ArrayList<>();
		int pageIndex=1;
		Row firstRow = null;
		if(rows.size()>0){
			firstRow = rows.get(0);
		}
		int firstRowCells = firstRow.getCells().size();
		for(int i=1;i<rows.size();i++){
			Row row = rows.get(i);
			int height=row.getRealHeight();
			if(height==0){
				continue;
			}
			Band band=row.getBand();
			if(band!=null){
				String rowKey=row.getRowKey();
				int index=-1;
				if(band.equals(Band.headerrepeat)){
					for(int j=0;j<pageRepeatHeaders.size();j++){
						Row headerRow=pageRepeatHeaders.get(j);
						if(headerRow.getRowKey().equals(rowKey)){
							index=j;
							break;
						}
					}
					pageRepeatHeaders.remove(index);
					pageRepeatHeaders.add(index,row);
				}else if(band.equals(Band.footerrepeat)){
					for(int j=0;j<pageRepeatFooters.size();j++){
						Row footerRow=pageRepeatFooters.get(j);
						if(footerRow.getRowKey().equals(rowKey)){
							index=j;
							break;
						}
					}
					pageRepeatFooters.remove(index);
					pageRepeatFooters.add(index,row);
				}
				continue;
			}
			int currentRowCells = row.getCells().size();

			if(firstRowCells==currentRowCells && i!=1){
				pageRows.add(0,firstRow);
				Page newPage=buildPage(pageRows,pageRepeatHeaders,pageRepeatFooters,titleRows,pageIndex,report);
				pageIndex++;
				pages.add(newPage);
				pageRows=new ArrayList<>();
			}
			pageRows.add(row);
		}
		if(pageRows.size()>0){
			pageRows.add(0,firstRow);
			Page newPage=buildPage(pageRows,pageRepeatHeaders,pageRepeatFooters,titleRows,pageIndex,report);
			pageIndex++;
			pages.add(newPage);
		}
		buildPageHeaderFooter(pages, report);
		buildSummaryRows(summaryRows, pages);
		return pages;
	}

}
