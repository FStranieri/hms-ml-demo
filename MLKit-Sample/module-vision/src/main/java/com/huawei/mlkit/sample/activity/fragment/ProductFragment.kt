/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.mlkit.sample.activity.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import com.huawei.hms.mlplugin.productvisionsearch.MLProductVisionSearchCapture.AbstractProductFragment
import com.huawei.hms.mlsdk.productvisionsearch.MLProductVisionSearch
import com.huawei.hms.mlsdk.productvisionsearch.MLVisionSearchProduct
import com.huawei.mlkit.sample.R
import com.huawei.mlkit.sample.activity.adapter.BottomSheetAdapter
import java.util.*

/**
 * Fragments of the result display of the product vision search, which is inherited from
 * MLProductVisionSearchCapture.AbstractUIExtendProxy and is used to display the information
 * detected by the product vision search plug-in interface.
 *
 * @since 2020-05-29
 */
class ProductFragment : AbstractProductFragment<MLProductVisionSearch>() {
    private lateinit var root: View
    private val mlProducts: MutableList<MLVisionSearchProduct> = ArrayList()
    private var productData: List<MLProductVisionSearch>? = null
    private lateinit var gridView: GridView
    private var adapter: BottomSheetAdapter? = null
    private lateinit var prompt: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = View.inflate(context, R.layout.fragment_product, null)
        initView()
        return root
    }

    override fun onResume() {
        super.onResume()
        product(productData)
    }

    private fun initView() {
        gridView = root.findViewById(R.id.gv)
        prompt = root.findViewById(R.id.prompt)
        gridView.numColumns = 2
        adapter = BottomSheetAdapter(mlProducts, context)
        root.findViewById<View>(R.id.img_close).setOnClickListener { activity!!.finish() }
        gridView.adapter = adapter
    }

    @Throws(Exception::class)
    override fun getProductList(list: List<MLProductVisionSearch>): List<MLProductVisionSearch> {
        return list
    }

    override fun onResult(productList: List<MLProductVisionSearch>) {
        productData = productList
        product(productList)
    }

    private fun product(productList: List<MLProductVisionSearch>?) {
        if (null == productList) {
            return
        }
        mlProducts.clear()
        if (productList.size == PRODUCT_NUM) {
            prompt.text = getString(R.string.empty_product)
            return
        }
        for (search in productList) {
            mlProducts.addAll(search.productList)
        }
        adapter!!.notifyDataSetChanged()
    }

    override fun onError(e: Exception): Boolean {
        Log.e(TAG, e.message!!)
        return false
    }

    companion object {
        private val TAG = ProductFragment::class.java.simpleName
        private const val PRODUCT_NUM = 0
    }
}