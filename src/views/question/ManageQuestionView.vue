<template>
  <div id="manageQuestionView">
    <h2>管理题目</h2>
  </div>
  <a-table
    :columns="columns"
    :data="dataList"
    :pagination="{
      showTotal: true,
      pageSize: searchParams.pageSize,
      current: searchParams.current,
      total,
    }"
    @page-change="onPageChange"
  >
    <template #optional="{ record }">
      <a-space>
        <a-button type="primary" status="warning" @click="doUpdate(record)"
          >修改</a-button
        >
        <a-button status="danger" @click="doDelete(record)">删除</a-button>
      </a-space>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import { onMounted, ref, watchEffect } from "vue";
import {
  type Page_Question_,
  type Question,
  QuestionControllerService,
} from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

// const show = ref(true);

const dataList = ref([]);
const total = ref(0);
const searchParams = ref({
  pageSize: 2,
  current: 1,
});
const loadData = async () => {
  const res = await QuestionControllerService.listQuestionByPageUsingPost(
    searchParams.value
  );
  if (res.code === 0) {
    dataList.value = res.data.records;
    total.value = res.data.total;
  } else {
    message.error("加载失败," + res.message);
  }
};

/**
 * 页面初始化时，请求数据
 */
onMounted(() => {
  loadData();
});

const columns = [
  {
    title: "id",
    dataIndex: "id",
  },
  {
    title: "标题",
    dataIndex: "title",
  },
  {
    title: "题目内容",
    dataIndex: "content",
  },
  {
    title: "标签",
    dataIndex: "tags",
  },
  {
    title: "答案",
    dataIndex: "answer",
  },
  {
    title: "提交数",
    dataIndex: "submitNum",
  },
  {
    title: "通过数",
    dataIndex: "acceptedNum",
  },
  {
    title: "判题用例",
    dataIndex: "judgeCase",
  },
  {
    title: "判题配置",
    dataIndex: "judgeConfig",
  },
  {
    title: "用户id",
    dataIndex: "userId",
  },
  {
    title: "创建时间",
    dataIndex: "createTime",
  },
  {
    title: "操作",
    slotName: "optional",
  },
];

const router = useRouter();
const doUpdate = (record: Question) => {
  console.log(record);
  router.push({
    path: "/update/question",
    // params是动态路由的参数，query才是url后面的参数
    query: {
      id: record.id,
    },
  });
};

const doDelete = async (record: Question) => {
  console.log(record);
  const res = await QuestionControllerService.deleteQuestionUsingPost({
    id: record.id,
  });
  if (res.code === 0) {
    message.success("删除成功");
    // 更新数据
    loadData();
  } else {
    message.error("删除失败," + res.message);
  }
};

const onPageChange = (page: number) => {
  searchParams.value = {
    //先把以前的老值复制过来
    ...searchParams.value,
    current: page,
  };
};

// 如果这个函数用到的变量发生改变时，就会自动的去触发这个函数
/**
 * 监听 searchParams 变量，改变时触发页面的重新加载
 */
watchEffect(() => {
  loadData();
});
</script>

<style scoped>
#manageQuestionView {
}
</style>
