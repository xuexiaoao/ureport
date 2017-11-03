/*******************************************************************************
 * Copyright (C) 2017 Bstek.com
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.bstek.ureport.build.paging;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.build.BindData;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.definition.HeaderFooterDefinition;
import com.bstek.ureport.model.Cell;
import com.bstek.ureport.model.Column;
import com.bstek.ureport.model.Report;
import com.bstek.ureport.model.Row;


/**
 * @author Jacky.gao
 * @since 2017年1月17日
 */
public abstract class BasePagination {
	protected void buildSummaryRows(List<Row> summaryRows,List<Page> pages){
		Page lastPage=pages.get(pages.size()-1);
		List<Row> lastPageRows=lastPage.getRows();
		int summaryRowSize=summaryRows.size()-1;
		for(int i=summaryRowSize;i>-1;i--){
			Row row=summaryRows.get(i);
			lastPageRows.add(row);
		}
	}
	protected Page buildPage(List<Row> rows,List<Row> headerRows,List<Row> footerRows,List<Row> titleRows,int pageIndex,Report report){
		int rowSize=rows.size();
		Row lastRow=rows.get(rowSize-1);
		int lastRowNumber=lastRow.getRowNumber();
		List<Column> columns=report.getColumns();
		Context context=report.getContext();
		context.setPageIndex(pageIndex);
		context.setCurrentPageRows(rows);
		Map<Row, Map<Column, Cell>> rowColCellsMap=report.getRowColCellMap();
		List<Row> reportRows=report.getRows();
		for(int i=0;i<rows.size();i++){
			Row row=rows.get(i);
			Map<Column,Cell> colMap=rowColCellsMap.get(row);
			if(colMap==null){
				continue;
			}
			for(Column col:columns){
				Cell cell=colMap.get(col);
				if(cell==null){
					continue;
				}
				buildExistPageFunctionCell(context, cell);
				int rowSpan=cell.getPageRowSpan();
				if(rowSpan==0){
					continue;
				}else{
					int span=rowSpan-1;
					int pageRowNumber=i+1;
					int maxRow=pageRowNumber+span;
					if(maxRow<=rowSize){
						continue;
					}else{
						Cell newCell=cell.newCell();
						newCell.setForPaging(true);
						int leftSpan=rowSize-pageRowNumber;
						if(leftSpan>0){
							leftSpan++;							
							cell.setPageRowSpan(leftSpan);
						}else{
							cell.setPageRowSpan(0);
						}
						newCell.setData(cell.getData());
						int newSpan=maxRow-rowSize;
						if(newSpan>1){
							newCell.setPageRowSpan(newSpan);
							newCell.setRowSpan(newSpan);
						}else{
							newCell.setPageRowSpan(0);		
							newCell.setRowSpan(0);
						}
						
						int nextRowNumber=lastRowNumber+1;
						Row nextRow=fetchNextRow(reportRows, nextRowNumber-1);
						newCell.setRow(nextRow);
						Map<Column,Cell> cmap=null;
						if(rowColCellsMap.containsKey(nextRow)){
							cmap=rowColCellsMap.get(nextRow);
						}else{
							cmap=new HashMap<Column,Cell>();
							rowColCellsMap.put(nextRow, cmap);
						}
						cmap.put(newCell.getColumn(),newCell);
					}
				}
			}
		}
		int headerRowSize=headerRows.size()-1;
		for(int i=headerRowSize;i>-1;i--){
			Row row=headerRows.get(i);
			Row newRow=duplicateRepeateRow(row, context);
			rows.add(0,newRow);
			Map<Column,Cell> colMap=rowColCellsMap.get(newRow);
			if(colMap==null){
				continue;
			}
			for(Column col:columns){
				Cell cell=colMap.get(col);
				if(cell==null){
					continue;
				}
				buildExistPageFunctionCell(context, cell);
			}
		}
		if(pageIndex==1){
			int titleRowSize=titleRows.size()-1;
			for(int i=titleRowSize;i>-1;i--){
				Row row=titleRows.get(i);
				rows.add(0,row);
				Map<Column,Cell> colMap=rowColCellsMap.get(row);
				if(colMap==null){
					continue;
				}
				for(Column col:columns){
					Cell cell=colMap.get(col);
					if(cell==null){
						continue;
					}
					buildExistPageFunctionCell(context, cell);
				}
			}
		}
		for(Row row:footerRows){
			Row newRow=duplicateRepeateRow(row, context);
			rows.add(newRow);
			Map<Column,Cell> colMap=rowColCellsMap.get(newRow);
			if(colMap==null){
				continue;
			}
			for(Column col:columns){
				Cell cell=colMap.get(col);
				if(cell==null){
					continue;
				}
				buildExistPageFunctionCell(context, cell);
			}
		}
		Page page=new Page(rows,columns);
		//modify by cooper 2017/10/24 11:42 start
		if(rows.size()>1){
			Row sheetRow = rows.get(1);
			int cellSize = sheetRow.getCells().size();
			buildSheetName(page,sheetRow,pageIndex,cellSize);
		}
		//modify by cooper 2017/10/24 11:42 start
		return page;
	}
	//add by cooper 2017/10/12 09:26 start
	private void buildSheetName(Page page,Row row,int pageIndex,int cellSize){
		if(pageIndex==1){
			page.setSheetName(String.valueOf(row.getCells().get(0).getData()));
		}else{
			page.setSheetName(String.valueOf(row.getCells().get(cellSize-1).getData()));
		}
	}
	//add by cooper 2017/10/12 09:26 end
	private Row fetchNextRow(List<Row> reportRows,int rowNumber){
		Row row=null;
		do{
			if(rowNumber>=reportRows.size()){
				break;
			}
			row=reportRows.get(rowNumber);
			rowNumber++;
		}while(row.getRealHeight()==0);
		return row;
	}

	private Row duplicateRepeateRow(Row row,Context context){
		Row newRow=row.newRow();
		Map<Row, Map<Column, Cell>> cellMap=context.getReport().getRowColCellMap();
		Map<Column, Cell> map=cellMap.get(row);
		if(map==null){
			return newRow;
		}
		Map<Column, Cell> newMap=new HashMap<Column,Cell>();
		cellMap.put(newRow, newMap);
		List<Column> columns=context.getReport().getColumns();
		for(Column col:columns){
			Cell cell=map.get(col);
			if(cell==null){
				continue;
			}
			Cell newCell=cell.newCell();
			newCell.setRow(newRow);
			newCell.setData(cell.getData());
			newCell.setCustomCellStyle(cell.getCustomCellStyle());
			newCell.setFormatData(cell.getFormatData());
			newCell.setExistPageFunction(cell.isExistPageFunction());
			newMap.put(col, newCell);
		}
		return newRow;
	}
	
	private void buildExistPageFunctionCell(Context context, Cell cell) {
		if(cell.isExistPageFunction()){
			List<BindData> dataList=context.buildCellData(cell);
			if(dataList==null || dataList.size()==0){
				return;
			}
			BindData bindData=dataList.get(0);
			cell.setData(bindData.getValue());
			cell.setBindData(bindData.getDataList());
			cell.doFormat();
			cell.doDataWrapCompute(context);
		}
	}
	
	protected void buildPageHeaderFooter(List<Page> pages,Report report){
		int totalPages=pages.size();
		for(int i=0;i<totalPages;i++){
			Page page=pages.get(i);
			HeaderFooterDefinition headerDef=report.getHeader();
			int pageIndex=i+1;
			if(headerDef!=null){
				HeaderFooter hf=headerDef.buildHeaderFooter(pageIndex, totalPages, report.getContext());
				page.setHeader(hf);
			}
			HeaderFooterDefinition footerDef=report.getFooter();
			if(footerDef!=null){
				HeaderFooter hf=footerDef.buildHeaderFooter(pageIndex, totalPages, report.getContext());
				page.setFooter(hf);
			}
		}
	}
}
