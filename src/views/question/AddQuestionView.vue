<template>
  <div id="addQuestionView">
    <h2>创建题目</h2>
    <a-form
      :model="form"
      :style="{ width: '600px' }"
      @submit="handleSubmit"
      label-align="left"
    >
      <a-form-item field="title" label="标题">
        <a-input v-model="form.title" placeholder="请输入标题" />
      </a-form-item>
      <a-form-item label="标签" :content-flex="false" :merge-props="false">
        <a-form-item
          v-for="(tag, index) of form.tags"
          :field="`form.tags[${index}].value`"
          :key="index"
          label-col-flex="100px"
        >
          <a-input v-model="form.tags[index]" placeholder="请输入标签" />
          <a-button
            status="danger"
            @click="handleDeleteTag(index)"
            :style="{ marginLeft: '5px' }"
            >删除</a-button
          >
        </a-form-item>
      </a-form-item>
      <div>
        <a-button @click="handleAddTag" type="outline" status="success"
          >新增标签</a-button
        >
      </div>
      <a-form-item field="content" label="题目内容">
        <MdEditor :value="form.content" :handle-change="onContentChange" />
      </a-form-item>
      <a-form-item field="answer" label="答案">
        <MdEditor :value="form.answer" :handle-change="onAnswerChange" />
      </a-form-item>
      <a-form-item>
        <a-button html-type="submit">Submit</a-button>
      </a-form-item>

      <a-form-item label="判题配置" :content-flex="false" :merge-props="false">
        <a-space direction="vertical" style="min-width: 480px">
          <a-form-item field="judgeConfig.timeLimit" label="时间限制">
            <a-input-number
              v-model="form.judgeConfig.timeLimit"
              placeholder="请输入时间限制"
              mode="button"
              min="0"
              size="large"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.stackLimit" label="堆栈限制">
            <a-input-number
              v-model="form.judgeConfig.stackLimit"
              placeholder="请输入堆栈限制"
              mode="button"
              min="0"
              size="large"
            />
          </a-form-item>
          <a-form-item field="judgeConfig.memoryLimit" label="内存限制">
            <a-input-number
              v-model="form.judgeConfig.memoryLimit"
              placeholder="请输入内存限制"
              mode="button"
              min="0"
              size="large"
            />
          </a-form-item>
        </a-space>
      </a-form-item>
      <!--动态表单-->
      <a-form-item
        label="测试用例配置"
        :content-flex="false"
        :merge-props="false"
      >
        <a-form-item
          v-for="(judgeCaseItem, index) of form.judgeCase"
          :label="`测试用例-${index}`"
          :key="index"
        >
          <a-space direction="vertical" style="min-width: 480px">
            <a-form-item
              :field="`from.judgeCase[${index}].input`"
              :label="`输入用例-${index}`"
              :key="index"
            >
              <a-input
                v-model="judgeCaseItem.input"
                placeholder="请输入测试用例"
              />
            </a-form-item>
            <a-form-item
              :field="`from.judgeCase[${index}].output`"
              :label="`输出用例-${index}`"
              :key="index"
            >
              <a-input
                v-model="judgeCaseItem.output"
                placeholder="请输出测试用例"
              />
            </a-form-item>
            <a-button status="danger" @click="handleDelete(index)"
              >删除
            </a-button>
          </a-space>
        </a-form-item>
        <div>
          <a-button @click="handleAdd" type="outline" status="success"
            >新增测试用例</a-button
          >
        </div>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" style="min-width: 200px" @click="doSubmit"
          >提交</a-button
        >
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive } from "vue";
import MdEditor from "@/components/MdEditor.vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";

const form = reactive({
  answer: "",
  content: "",
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
  judgeConfig: {
    memoryLimit: 0,
    stackLimit: 0,
    timeLimit: 0,
  },
  tags: ["容易", "简单", "中等", "较难", "困难"],
  title: "",
});

/**
 * 新增测试用例
 */
const handleAdd = () => {
  form.judgeCase.push({
    input: "",
    output: "",
  });
};
/**
 * 删除测试用例
 * @param index
 */
const handleDelete = (index: number) => {
  form.judgeCase.splice(index, 1);
};

const doSubmit = async () => {
  console.log(form);
  const res = await QuestionControllerService.addQuestionUsingPost(form);
  if (res.code === 0) {
    message.success("创建题目成功");
  } else {
    message.error("创建失败" + res.msg);
  }
};

const handleAddTag = () => {
  form.tags.push("");
};

const handleDeleteTag = (index: number) => {
  form.tags.splice(index, 1);
};

const onContentChange = (value: string) => {
  form.content = value;
};

const onAnswerChange = (value: string) => {
  form.answer = value;
};

const colors = [
  "orangered",
  "orange",
  "gold",
  "lime",
  "green",
  "cyan",
  "blue",
  "arcoblue",
  "purple",
  "pinkpurple",
  "magenta",
  "gray",
];
</script>

<style scoped>
#addQuestionView {
}
</style>
