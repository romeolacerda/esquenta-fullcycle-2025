import { Injectable } from '@nestjs/common';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreatePostDto } from './dto/create-post.dto';
import { UpdatePostDto } from './dto/update-post.dto';

@Injectable()
export class PostsService {
  constructor(private prismaService: PrismaService){}

  create(createPostDto: CreatePostDto & {authorId: string}) {
    return this.prismaService.post.create({
      data: createPostDto
    })
  }

  findAll() {
    return this.prismaService.post.findMany();
  }

  findOne(id: string) {
    return this.prismaService.post.findMany({
      where: {id}
    });
  }

  update(id: string, updatePostDto: UpdatePostDto) {
    return this.prismaService.post.update({
      where:{id},
      data: updatePostDto
    });
  }

  remove(id: string) {
    return this.prismaService.post.delete({
      where: {id}

    });

  }
}
