/**
 * Created by Jacky.Gao on 2017-02-04.
 */
import DatabaseTree from '../tree/DatabaseTree.js';
import SpringTree from '../tree/SpringTree.js';
import BuildinTree from '../tree/BuildinTree.js';
import DatasourceDialog from '../dialog/DatasourceDialog.js';
import SpringDialog from '../dialog/SpringDialog.js';
import BuildinDatasourceSelectDialog from '../dialog/BuildinDatasourceSelectDialog.js';

export default class DatasourcePanel{
    constructor(context){
        this.context=context;
        context.datasourcePanel=this;
        const reportDef=context.reportDef;
        if(!reportDef.datasources){
            reportDef.datasources=[];
        }
        this.datasources=reportDef.datasources;
    }
    buildPanel(){
        const panel=$(`<div style="width:100%;"></div>`);
        const toolbar=$(`<div class="btn-group ud-toolbar"></div>`);
        panel.append(toolbar);

        this.treeContainer=$(`<div style="height: 500px;overflow: auto">`);
        panel.append(this.treeContainer);

        const _this=this;

        const addBuildinBtn=$(`<button class="btn btn-default" style="border:none;border-radius:0;background: #f8f8f8;padding: 6px 8px;" title="添加内置数据源连接">
            <i class="ureport ureport-shareconnection"></i>
        </button>`);
        toolbar.append(addBuildinBtn);
        const buildinDatasourceSelectDialog=new BuildinDatasourceSelectDialog(this.datasources);
        addBuildinBtn.click(function(){
            buildinDatasourceSelectDialog.show(function(name){
                const ds={name};
                const tree=new BuildinTree(_this.treeContainer,_this.datasources,ds,_this.context);
                _this.datasources.push(tree);
            });
        });
        this.buildDatasources();
        return panel;
    }

    buildDatasources(){
        this.treeContainer.empty();
        for(let ds of this.datasources){
            if(ds.type==='jdbc'){
                new DatabaseTree(this.treeContainer,this.datasources,ds,this.datasourceDialog,this.context);
            }else if(ds.type==='spring'){
                new SpringTree(this.treeContainer,this.datasources,ds,this.springDialog,this.context);
            }else if(ds.type==='buildin'){
                new BuildinTree(this.treeContainer,this.datasources,ds,this.context);
            }
        }
    }
}