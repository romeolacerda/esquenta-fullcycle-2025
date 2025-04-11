import { accessibleBy } from '@casl/prisma';
import { Injectable } from '@nestjs/common';
import { CaslAbilityService } from 'src/casl/casl-ability/casl-ability.service';
import { PrismaService } from 'src/prisma/prisma.service';
import { CreatePostDto } from './dto/create-post.dto';
import { UpdatePostDto } from './dto/update-post.dto';

@Injectable()
export class PostsService {
  constructor(private prismaService: PrismaService, private abilityService: CaslAbilityService){}

  create(createPostDto: CreatePostDto & {authorId: string}) {
    const ability = this.abilityService.ability

    if(!ability.can('create', 'Post')){
      throw new Error('Unauthorized')
    }
    return this.prismaService.post.create({
      data: createPostDto
    })
  }

  findAll() {
    const ability = this.abilityService.ability

    return this.prismaService.post.findMany({
      where:{
        AND: [accessibleBy(ability, 'read').Post]
      }
    });
  }

  findOne(id: string) {
    return this.prismaService.post.findUnique({
      where: {
        id,
        AND: [accessibleBy(this.abilityService.ability, 'read').Post]
      }
    });
  }

  async update(id: string, updatePostDto: UpdatePostDto) {

    const post = await this.prismaService.post.findUnique({
      where: {
        id,
        AND: [accessibleBy(this.abilityService.ability, 'read').Post]
      }
    });

    if(!post){
      throw new Error('Post not found')
    }

    return this.prismaService.post.update({
      where:{id},
      data: updatePostDto
    });
  }

  async remove(id: string) {
    const post = await this.prismaService.post.findUnique({
      where: {
        id,
        AND: [accessibleBy(this.abilityService.ability, 'read').Post]
      }
    });

    if(!post){
      throw new Error('Post not found')
    }

    return this.prismaService.post.delete({
      where: {id}

    });

  }
}
