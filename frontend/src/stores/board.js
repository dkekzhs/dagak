import { ref } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'

// export function getPosts() {
//   return posts
// }

export const useBoardStore = defineStore(
  'boardStore',
  () => {
    const posts = [
      { id: 1, title: '제목1', content: '내용1', createdAt: '2021-01-01' },
      { id: 2, title: '제목2', content: '내용2', createdAt: '2022-02-02' },
      { id: 3, title: '제목3', content: '내용3', createdAt: '2023-03-03' },
      { id: 4, title: '제목4', content: '내용4', createdAt: '2024-04-04' },
      { id: 5, title: '제목5', content: '내용5', createdAt: '2025-05-05' }
    ]

    const getPosts = async () => {
      return posts
    }
    // const getPosts = async function () {
    //   try {
    //     const response = await axios.get(`${import.meta.env.VITE_API_BASE_URL}/`);

    //   } catch (error) {
    //     console.error(error);
    //   }
    // };

    return { posts, getPosts }
  },
  { persist: true }
)